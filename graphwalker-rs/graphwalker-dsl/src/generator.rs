use std::time::Duration;

use graphwalker_core::condition::StopCondition;
use graphwalker_core::generator::PathGenerator;

#[derive(Clone, Debug, PartialEq, Eq)]
pub enum DslError {
    UnexpectedEnd,
    Expected(String),
    UnknownGenerator(String),
    UnknownCondition(String),
    InvalidNumber(String),
}

impl std::fmt::Display for DslError {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            Self::UnexpectedEnd => write!(f, "Unexpected end of input"),
            Self::Expected(msg) => write!(f, "Expected {}", msg),
            Self::UnknownGenerator(name) => write!(f, "Unknown generator: {}", name),
            Self::UnknownCondition(name) => write!(f, "Unknown stop condition: {}", name),
            Self::InvalidNumber(s) => write!(f, "Invalid number: {}", s),
        }
    }
}

impl std::error::Error for DslError {}

struct Parser<'a> {
    input: &'a str,
    pos: usize,
}

impl<'a> Parser<'a> {
    fn new(input: &'a str) -> Self {
        Self { input, pos: 0 }
    }

    fn skip_whitespace(&mut self) {
        while self.pos < self.input.len() && self.input.as_bytes()[self.pos].is_ascii_whitespace() {
            self.pos += 1;
        }
    }

    fn peek(&self) -> Option<u8> {
        self.input.as_bytes().get(self.pos).copied()
    }

    fn expect(&mut self, ch: u8) -> Result<(), DslError> {
        self.skip_whitespace();
        match self.peek() {
            Some(c) if c == ch => {
                self.pos += 1;
                Ok(())
            }
            _ => Err(DslError::Expected(format!("'{}'", ch as char))),
        }
    }

    fn read_identifier(&mut self) -> Result<String, DslError> {
        self.skip_whitespace();
        let start = self.pos;
        while self.pos < self.input.len() {
            let b = self.input.as_bytes()[self.pos];
            if b.is_ascii_alphanumeric() || b == b'_' {
                self.pos += 1;
            } else {
                break;
            }
        }
        if self.pos == start {
            return Err(DslError::UnexpectedEnd);
        }
        Ok(self.input[start..self.pos].to_string())
    }

    fn read_number(&mut self) -> Result<u64, DslError> {
        self.skip_whitespace();
        let start = self.pos;
        while self.pos < self.input.len() && self.input.as_bytes()[self.pos].is_ascii_digit() {
            self.pos += 1;
        }
        if self.pos == start {
            return Err(DslError::Expected("number".to_string()));
        }
        self.input[start..self.pos]
            .parse()
            .map_err(|_| DslError::InvalidNumber(self.input[start..self.pos].to_string()))
    }

    fn read_string_arg(&mut self) -> Result<String, DslError> {
        self.skip_whitespace();
        let start = self.pos;
        while self.pos < self.input.len() {
            let b = self.input.as_bytes()[self.pos];
            if b == b')' {
                break;
            }
            self.pos += 1;
        }
        let s = self.input[start..self.pos].trim().to_string();
        if s.is_empty() {
            return Err(DslError::Expected("string argument".to_string()));
        }
        Ok(s)
    }

    fn at_end(&mut self) -> bool {
        self.skip_whitespace();
        self.pos >= self.input.len()
    }

    fn parse_stop_condition(&mut self) -> Result<StopCondition, DslError> {
        let name = self.read_identifier()?;
        let lower = name.to_ascii_lowercase();

        match lower.as_str() {
            "never" => Ok(StopCondition::Never),
            "predefined_path" | "predefinedpath" => Ok(StopCondition::PredefinedPath),

            "edge_coverage" | "edgecoverage" => {
                self.expect(b'(')?;
                let n = self.read_number()? as u32;
                self.expect(b')')?;
                Ok(StopCondition::EdgeCoverage(n))
            }
            "vertex_coverage" | "vertexcoverage" => {
                self.expect(b'(')?;
                let n = self.read_number()? as u32;
                self.expect(b')')?;
                Ok(StopCondition::VertexCoverage(n))
            }
            "requirement_coverage" | "requirementcoverage" => {
                self.expect(b'(')?;
                let n = self.read_number()? as u32;
                self.expect(b')')?;
                Ok(StopCondition::RequirementCoverage(n))
            }
            "dependency_edge_coverage" | "dependencyedgecoverage" => {
                self.expect(b'(')?;
                let n = self.read_number()? as u32;
                self.expect(b')')?;
                Ok(StopCondition::DependencyEdgeCoverage(n))
            }
            "reached_vertex" | "reachedvertex" => {
                self.expect(b'(')?;
                let name = self.read_string_arg()?;
                self.expect(b')')?;
                Ok(StopCondition::reached_vertex(name))
            }
            "reached_edge" | "reachededge" => {
                self.expect(b'(')?;
                let name = self.read_string_arg()?;
                self.expect(b')')?;
                Ok(StopCondition::reached_edge(name))
            }
            "reached_shared_state" | "reachedsharedstate" => {
                self.expect(b'(')?;
                let name = self.read_string_arg()?;
                self.expect(b')')?;
                Ok(StopCondition::reached_shared_state(name))
            }
            "time_duration" | "timeduration" => {
                self.expect(b'(')?;
                let secs = self.read_number()?;
                self.expect(b')')?;
                Ok(StopCondition::time_duration(Duration::from_secs(secs)))
            }
            "length" => {
                self.expect(b'(')?;
                let n = self.read_number()?;
                self.expect(b')')?;
                Ok(StopCondition::Length(n))
            }
            _ => Err(DslError::UnknownCondition(name)),
        }
    }

    // logicalExpression = primaryExpression ((AND | OR) primaryExpression)*
    fn parse_logical_expression(&mut self) -> Result<StopCondition, DslError> {
        let first = self.parse_stop_condition()?;
        self.skip_whitespace();

        let mut conditions = vec![first];
        let mut combinator: Option<bool> = None; // true = AND, false = OR

        loop {
            self.skip_whitespace();
            if self.peek() == Some(b')') || self.at_end() {
                break;
            }

            let saved_pos = self.pos;
            let is_and = self.try_parse_combinator();

            match is_and {
                Some(and) => {
                    if let Some(existing) = combinator {
                        if existing != and {
                            return Err(DslError::Expected(
                                "consistent AND/OR (cannot mix)".to_string(),
                            ));
                        }
                    }
                    combinator = Some(and);
                    conditions.push(self.parse_stop_condition()?);
                }
                None => {
                    self.pos = saved_pos;
                    break;
                }
            }
        }

        if conditions.len() == 1 {
            Ok(conditions.into_iter().next().unwrap())
        } else if combinator == Some(true) {
            Ok(StopCondition::Combined(conditions))
        } else {
            Ok(StopCondition::Alternative(conditions))
        }
    }

    // returns Some(true) for AND, Some(false) for OR, None if neither
    fn try_parse_combinator(&mut self) -> Option<bool> {
        self.skip_whitespace();

        if self.pos + 1 < self.input.len() && &self.input[self.pos..self.pos + 2] == "&&" {
            self.pos += 2;
            return Some(true);
        }
        if self.pos + 1 < self.input.len() && &self.input[self.pos..self.pos + 2] == "||" {
            self.pos += 2;
            return Some(false);
        }

        let saved = self.pos;
        if let Ok(ident) = self.read_identifier() {
            match ident.to_ascii_lowercase().as_str() {
                "and" => return Some(true),
                "or" => return Some(false),
                _ => {}
            }
        }
        self.pos = saved;
        None
    }

    fn parse_single_generator(&mut self) -> Result<PathGenerator, DslError> {
        let name = self.read_identifier()?;
        let lower = name.to_ascii_lowercase();
        self.expect(b'(')?;

        if matches!(
            lower.as_str(),
            "new_york_street_sweeper" | "newyorkstreetsweeper"
        ) {
            self.expect(b')')?;
            return Ok(PathGenerator::new_york_street_sweeper());
        }

        let condition = self.parse_logical_expression()?;
        self.expect(b')')?;

        match lower.as_str() {
            "random" | "random_path" | "randompath" => Ok(PathGenerator::random(condition)),
            "quick_random" | "quick_random_path" | "quickrandom" | "quickrandompath" => {
                Ok(PathGenerator::quick_random(condition))
            }
            "weighted_random" | "weighted_random_path" | "weightedrandompath" => {
                Ok(PathGenerator::weighted_random(condition))
            }
            "a_star" | "astar" | "astarpath" => Ok(PathGenerator::a_star(condition)),
            "shortest_all_paths" | "shortestallpaths" => {
                Ok(PathGenerator::shortest_all_paths(condition))
            }
            "predefined_path" | "predefinedpath" => Ok(PathGenerator::predefined(condition)),
            _ => Err(DslError::UnknownGenerator(name)),
        }
    }
}

/// Parse a generator DSL expression into a `PathGenerator`.
///
/// Supports single generators like `random(edge_coverage(100))` and
/// multiple generators separated by whitespace, which produce a `CombinedPath`.
/// Stop conditions can be combined with AND (`&&`/`and`) or OR (`||`/`or`).
pub fn parse_generator(input: &str) -> Result<PathGenerator, DslError> {
    let mut parser = Parser::new(input);

    let first = parser.parse_single_generator()?;

    if parser.at_end() {
        return Ok(first);
    }

    let mut generators = vec![first];
    while !parser.at_end() {
        generators.push(parser.parse_single_generator()?);
    }

    Ok(PathGenerator::combined(generators))
}

#[cfg(test)]
mod tests;
