# Migration

---

## Migrating from `5.x.x` versions to `6.x.x` versions

Test Engine `6.x.x` versions changed annotations that require values.

### @TestEngine.DisplayName

`5.x.x` code:

```java
public class Test {
    
  // ... code omitted ...
  
  @TestEngine.Test
  @TestEngine.DisplayName("some display name")
  public void test() {
    // ... code omitted ...
  }
}
```

`6.x.x` code:

```java
public class Test {
    
  // ... code omitted ...

  @TestEngine.Test
  @TestEngine.DisplayName(name = "some display name")
  public void test() {
    // ... code omitted ...
  }
}
```

---

### @TestEngine.Order

`5.x.x` code:

```java
public class Test {

  // ... code omitted ...

  @TestEngine.Test
  @TestEngine.Order(1)
  public void test() {
    // ... code omitted ...
  }
}
```

`6.x.x` code:

```java
public class Test {

  // ... code omitted ...
  
  @TestEngine.Test
  @TestEngine.Order(hierarchyTraversalOrder = 1)
  public void test() {
    // ... code omitted ...
  }
}
```

---

### @TestEngine.Tag

`5.x.x` code:

```java
public class Test {

  // ... code omitted ...

  @TestEngine.Test
  @TestEngine.Tag("/some-tag/")
  public void test() {
    // ... code omitted ...
  }
}
```

`6.x.x` code:

```java
public class Test {

  // ... code omitted ...
  
  @TestEngine.Test
  @TestEngine.Tag(tag = "/some-tag/")
  public void test() {
    // ... code omitted ...
  }
}
```

### @TestEngine.Lock

`5.x.x` code:

```java
public class Test {

  // ... code omitted ...

  @TestEngine.Test
  public void test() {
    // ... code omitted ...
  }
}
```

`6.x.x` code:

```java
public class Test {

  // ... code omitted ...
  
  @TestEngine.Test
  public void test() {
    // ... code omitted ...
  }
}
```

### @TestEngine.Unlock

`5.x.x` code:

```java
public class Test {

  // ... code omitted ...

  @TestEngine.Test
  public void test() {
    // ... code omitted ...
  }
}
```

`6.x.x` code:

```java
public class Test {

  // ... code omitted ...
  
  @TestEngine.Test
  public void test() {
    // ... code omitted ...
  }
}
```

### Store

The `Store` object has been refactored to allow both global usage and local test class usage.

Previous static methods have been removed; use `Store.getSingleton()`

`5.x.x` code:

```java
public class Test {

  // ... code omitted ...

  @TestEngine.Test
  public void test() {
    // ... code omitted ...
      
    String value = Store.get("key");
  }
}
```

`6.x.x` code:

`Store` now has `getInstance()` to get a singleton instance.

- `singleton()` has been deprecated
- `getSingleton()` has been deprecated

```java
public class Test {

  // ... code omitted ...
  
  @TestEngine.Test
  public void test() {
    // ... code omitted ...
    
    String value = Store.getInstance().get("key");
  }
}
```

`Directory` has been moved to the `extras` module.

`LineSource` has been moved to the `extras` module.

## Migrating from `6.x.x` versions to `7.x.x` versions

### Arguments

The `Named` generic interface allows for naming arguments. 

Example:

```java
public class SimpleTest1 {

    @TestEngine.Argument
    protected Named<String> argument;

    @TestEngine.ArgumentSupplier
    public static Stream<Named<String>> arguments() {
        Collection<NamedString> collection = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            collection.add(Named.of("StringArgument " + i, "string value " + i));
        }
        return collection.stream();
    }

    // ... code omitted ...
}
```

Plain Objects:

If the `@TestEngine.ArgumentSupplier` returns a `Stream` or `Iterable` containing non-`Named` objects, then the test engine will wrap each object with a `Named` using the objects position in the stream/iterable as the name.

Example:

```java
public class SimpleTest1 {

    @TestEngine.Argument
    protected String argument;

    @TestEngine.ArgumentSupplier
    public static Stream<String> arguments() {
        Collection<String> collection = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            collection.add("string value " + i);
        }
        return collection.stream();
    }

    // ... code omitted ...
}
```

### Store

`Store` has been removed.

### Locking

`@TestEngine.ResourceLock`, `@TestEngine.Lock`, and `@TestEngine.Unlock` have been removed and replaced with a `Lock` class.

Why?

- Code clarity
- Code logic via annotation seems like an anti-pattern
- `Lock` allows for more complex and robust scenarios