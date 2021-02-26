package org.graphwalker.java.source;

/*
 * #%L
 * GraphWalker Java
 * %%
 * Copyright (C) 2005 - 2014 GraphWalker
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import japa.parser.ASTHelper;
import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.ModifierSet;
import japa.parser.ast.comments.LineComment;
import japa.parser.ast.expr.*;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import org.graphwalker.core.machine.Context;
import org.graphwalker.io.factory.ContextFactory;
import org.graphwalker.io.factory.ContextFactoryScanner;
import org.graphwalker.java.source.cache.CacheEntry;
import org.graphwalker.java.source.cache.SimpleCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.graphwalker.core.model.Model.RuntimeModel;

/**
 * @author Nils Olsson
 */
public final class CodeGenerator extends VoidVisitorAdapter<ChangeContext> {

  private static final Logger logger = LoggerFactory.getLogger(CodeGenerator.class);
  private static CodeGenerator generator = new CodeGenerator();

  public static void generate(final Path input, final Path output) {
    final SimpleCache cache = new SimpleCache(output);
    try {
      Files.walkFileTree(input, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
          if (!cache.contains(file) || isModified(file)) {
            try {
              ContextFactory factory = ContextFactoryScanner.get(file);
              List<Context> contexts = factory.create(file);
              logger.info("Source generated from: " + file.toString() + " -> ");
              for (Context context : contexts) {
                SourceFile sourceFile = new SourceFile(context.getModel().getName(), file, input, output);
                write(context, sourceFile);
                cache.add(file, new CacheEntry(file.toFile().lastModified(), true));
                logger.info("                       " + sourceFile.getOutputPath());
              }
            } catch (Throwable t) {
              logger.info(t.getMessage());
              cache.add(file, new CacheEntry(file.toFile().lastModified(), false));
            }
          }
          return FileVisitResult.CONTINUE;
        }

        private boolean isModified(Path file) throws IOException {
          return !Files.getLastModifiedTime(file).equals(cache.get(file).getLastModifiedTime());
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
          cache.add(file, new CacheEntry(file.toFile().lastModified(), false));
          return FileVisitResult.CONTINUE;
        }
      });
    } catch (IOException e) {
      logger.error(e.getMessage());
      throw new CodeGeneratorException(e);
    }
  }

  private static void write(Context context, SourceFile file) throws IOException {
    try {
      RuntimeModel model = context.getModel();
      String source = generator.generate(file, model);
      Files.createDirectories(file.getOutputPath().getParent());
      Files.write(file.getOutputPath(), source.getBytes(Charset.forName("UTF-8"))
        , StandardOpenOption.CREATE
        , StandardOpenOption.TRUNCATE_EXISTING);
    } catch (Throwable t) {
      logger.error(t.getMessage());
      throw new CodeGeneratorException(t);
    }
  }

  public List<String> generate(String file) throws IOException {
    return generate(Paths.get(file));
  }

  public List<String> generate(File file) throws IOException {
    return generate(file.toPath());
  }

  public List<String> generate(Path path) throws IOException {
    ContextFactory factory = ContextFactoryScanner.get(path);
    List<Context> contexts = factory.create(path);
    List<String> sources = new ArrayList<>();
    for (Context context : contexts) {
      String sourceStr = "";
      SourceFile sourceFile = new SourceFile(context.getModel().getName(), path);
      sourceStr += generate(sourceFile, context.getModel());
      sources.add(sourceStr);
    }
    return sources;
  }

  public String generate(SourceFile sourceFile, RuntimeModel model) {
    CompilationUnit compilationUnit = getCompilationUnit(sourceFile);
    ChangeContext changeContext = new ChangeContext(model);
    visit(compilationUnit, changeContext);
    removeMethods(compilationUnit, changeContext);
    generateMethods(compilationUnit, changeContext);
    return compilationUnit.toString();
  }

  private CompilationUnit getCompilationUnit(SourceFile sourceFile) {
    CompilationUnit compilationUnit;
    if (Files.exists(sourceFile.getOutputPath())) {
      try {
        compilationUnit = JavaParser.parse(sourceFile.getOutputPath().toFile());
      } catch (Throwable t) {
        logger.error(t.getMessage());
        throw new RuntimeException(t);
      }
    } else {
      compilationUnit = new CompilationUnit();
      compilationUnit.setComment(new LineComment(" Generated by GraphWalker (http://www.graphwalker.org)"));
      if (!"".equals(sourceFile.getPackageName())) {
        compilationUnit.setPackage(createPackageDeclaration(sourceFile));
      }
      compilationUnit.setImports(Arrays.asList(
        new ImportDeclaration(new NameExpr("org.graphwalker.java.annotation.Model"), false, false),
        new ImportDeclaration(new NameExpr("org.graphwalker.java.annotation.Vertex"), false, false),
        new ImportDeclaration(new NameExpr("org.graphwalker.java.annotation.Edge"), false, false)
      ));
      ASTHelper.addTypeDeclaration(compilationUnit, getInterfaceName(sourceFile));
    }
    return compilationUnit;
  }

  private void removeMethods(CompilationUnit compilationUnit, ChangeContext changeContext) {
    if (0 < changeContext.getMethodDeclarations().size()) {
      ClassOrInterfaceDeclaration body = (ClassOrInterfaceDeclaration) compilationUnit.getTypes().get(0);
      body.getMembers().removeAll(changeContext.getMethodDeclarations());
    }
  }

  private void generateMethods(CompilationUnit compilationUnit, ChangeContext changeContext) {
    ClassOrInterfaceDeclaration body = (ClassOrInterfaceDeclaration) compilationUnit.getTypes().get(0);
    for (String methodName : changeContext.getMethodNames()) {
      if (isValidName(methodName)) {
        MethodDeclaration method = new MethodDeclaration(Modifier.INTERFACE, ASTHelper.VOID_TYPE, methodName);
        List<AnnotationExpr> annotations = new ArrayList<>();
        if (changeContext.isVertex(methodName)) {
          List<MemberValuePair> memberValuePairs = new ArrayList<>();
          annotations.add(new NormalAnnotationExpr(ASTHelper.createNameExpr("Vertex"), memberValuePairs));
        } else {
          List<MemberValuePair> memberValuePairs = new ArrayList<>();
          annotations.add(new NormalAnnotationExpr(ASTHelper.createNameExpr("Edge"), memberValuePairs));
        }
        method.setAnnotations(annotations);
        ASTHelper.addMember(body, method);
      }
    }
  }

  private boolean isValidName(String name) {
    if (null == name || name.isEmpty()) {
      return false;
    }
    boolean valid = true;
    for (int i = 0; i < name.length(); i++) {
      if (0 == i) {
        valid &= Character.isJavaIdentifierStart(name.charAt(i));
      } else {
        valid &= Character.isJavaIdentifierPart(name.charAt(i));
      }
    }
    return valid;
  }

  public void visit(MethodDeclaration methodDeclaration, ChangeContext changeContext) {
    if (changeContext.getMethodNames().contains(methodDeclaration.getName())) {
      changeContext.getMethodNames().remove(methodDeclaration.getName());
    } else {
      changeContext.addMethodDeclaration(methodDeclaration);
    }
  }

  private PackageDeclaration createPackageDeclaration(SourceFile sourceFile) {
    return new PackageDeclaration(ASTHelper.createNameExpr(sourceFile.getPackageName()));
  }

  private ClassOrInterfaceDeclaration getInterfaceName(SourceFile sourceFile) {
    ClassOrInterfaceDeclaration classOrInterfaceDeclaration = new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, false, sourceFile.getClassName());
    List<MemberValuePair> memberValuePairs = new ArrayList<>();
    memberValuePairs.add(new MemberValuePair("file", new StringLiteralExpr(sourceFile.getRelativePath().toString().replace(File.separator, "/"))));
    List<AnnotationExpr> annotations = new ArrayList<>();
    annotations.add(new NormalAnnotationExpr(ASTHelper.createNameExpr("Model"), memberValuePairs));
    classOrInterfaceDeclaration.setAnnotations(annotations);
    classOrInterfaceDeclaration.setInterface(true);
    return classOrInterfaceDeclaration;
  }
}
