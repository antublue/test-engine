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

### Store

Deprecated `Store` methods have been removed.

- `singleton()` has been removed
- `getSingleton()` has been removed