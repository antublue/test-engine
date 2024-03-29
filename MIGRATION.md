# Migration

---

### Migrating from `5.x.x` versions to `6.x.x` versions

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
  @TestEngine.Lock("lock-name")
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
  @TestEngine.Lock(name = "lock-name")
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
  @TestEngine.Unlock("lock-name")
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
  @TestEngine.Unlock(name = "lock-name")
  public void test() {
    // ... code omitted ...
  }
}
```

## Store

The `Store` object has been refactored to allow both global usage and local test class usage.

Previous static methods have been removed; use `Store.singleton()`

`5.x.x` code:

```java
public class Test {

  // ... code omitted ...

  @TestEngine.Test
  @TestEngine.Unlock("lock-name")
  public void test() {
    // ... code omitted ...
      
    String value = Store.get("key");
  }
}
```

`6.x.x` code:

```java
public class Test {

  // ... code omitted ...
  
  @TestEngine.Test
  @TestEngine.Unlock(name = "lock-name")
  public void test() {
    // ... code omitted ...
    
    String value = Store.singleton().get("key");
  }
}
```