
## Chain multiple modifications on the same element

```
// No:
$('#requirements').val("");
$('#requirements').textinput('disable');

// Yes:
$('#requirements').val("").textinput('disable');
```

This saves an element lookup.

## Don't use global scope

Everything is in the global scope. This makes the app _very_ vulnerable to conflicts with 3rd-party code.

## Save reference to elements instead of doing DOM lookups every time you need them

```
// No:
document.getElementById("runModel").disabled = true;
document.getElementById("pausePlayExecution").disabled = false;
document.getElementById("pausePlayExecution").innerHTML = "Pause";

// Yes:
var runModel = document.getElementById("runModel");
var pausePlayExecution = document.getElementById("pausePlayExecution");
var pausePlayExecution = document.getElementById("pausePlayExecution");

runModel.disabled = true;
pausePlayExecution.disabled = false;
pausePlayExecution.innerHTML = "Pause";
```

- Now you can re-use those variables as many times as you want without doing another DOM lookup
- If the element ID changes, you only need to update it in one place
- Less code :)

## jQuery vs native code

Be consistent in how you code; The same things are done in both jQuery and vanilla JS.

Examples: DOM lookups (`$('#foo')` vs `document.getElementById('foo')`), event handlers

## Break down into external JS files

- JS should be in `.js` files
- Break down into different modules would make the code more maintainable (and unit-testable)

## Use strict comparisons

```
// No:
if (foo == 'bar') {
// Yes:
if (foo === 'bar') {
```

This prevents JS from doing type conversions before comparing, which you usually don't need. More info: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/Comparison_Operators#Using_the_Equality_Operators

## Don't initialize variables with `undefined` value

```
// No:
var foo = undefined;
// Yes:
var foo;
```

Declaring a variable but _not_ assigning it a value will initialize it with the value `undefined`, so there's no need to specify it.

## Store reference to re-used object data

This is done in a few places. Here's one:

```
// No:
var edge = {
    id: edge.data().id,
    name: edge.data().label,
    guard: edge.data().guard,
    actions: actions,
    requirements: requirements,
    properties: edge.data().properties,
    sourceVertexId: edge.data().source,
    targetVertexId: edge.data().target
};

// Yes:
var data = edge.data();
var edge = {
    id: data.id,
    name: data.label,
    guard: data.guard,
    actions: actions,
    requirements: requirements,
    properties: data.properties,
    sourceVertexId: data.source,
    targetVertexId: data.target
};
```

This is a micro-optimisation. But as you don't expect the result of `edge.data()` to change between calls you might as well run it once and re-use the result.

## Use a linter for consistent styling

I recommend http://eslint.org/

