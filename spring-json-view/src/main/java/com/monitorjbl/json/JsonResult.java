package com.monitorjbl.json;

public class JsonResult {
  private static final JsonResult instance = new JsonResult();
  private static final ThreadLocal<JsonView> current = new ThreadLocal<>();

  private JsonResult() {
  }

  /**
   * Use the provided {@code JsonView} object to serialize
   * the return value.
   *
   * @param view JsonView used to render JSON
   * @param <E> Type of object being rendered
   * @return ResultWrapper to provide return value
   */
  @SuppressWarnings("unchecked")
  public <E> ResultWrapper<E> use(JsonView<E> view) {
    current.set(view);
    return new ResultWrapper<>(view);
  }

  public static JsonResult instance() {
    return instance;
  }

  static JsonView get() {
    return current.get();
  }

  static void unset(){
    current.remove();
  }

  public static class ResultWrapper<T> {
    private JsonView<T> obj;

    private ResultWrapper(JsonView<T> obj) {
      this.obj = obj;
    }

    /**
     * Returns the object being serialized
     *
     * @return the object
     */
    public T returnValue() {
      return obj.getValue();
    }
  }
}
