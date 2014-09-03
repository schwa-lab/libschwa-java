package org.schwa.dr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


/**
 * Implementation of docrep stores; ordered collections of annotation instances. This class wraps
 * an {@link ArrayList} while providing some additional methods, such as {@link Store#create}, and
 * adding some additional docrep semantics to methods like {@link Store#add} and
 * {@link Store#clear}.
 *
 * @author Tim Dawborn
 **/
public class Store<T extends Ann> implements List<T> {
  protected final List<T> items;

  public Store() {
    items = new ArrayList<T>();
  }

  /**
   * Adds a new {@link Ann} instance to the store, setting the drIndex attribute in the process.
   **/
  @Override
  public boolean add(T obj) {
    if (obj.getDRIndex() != null)
      throw new IllegalArgumentException("Cannot insert an object into a Store which is already in a store (drIndex=" + obj.getDRIndex() + ")");
    obj.setDRIndex(items.size());
    return items.add(obj);
  }

  /**
   * This method is unsupported but required by the {@link List} interface.
   * @throws UnsupportedOperationException
   **/
  @Override
  public void add(int index, T o) {
    throw new UnsupportedOperationException();
  }

  /**
   * @see Store#add(Ann)
   **/
  @Override
  public boolean addAll(Collection<? extends T> c) {
    for (T obj : c)
      if (obj.getDRIndex() != null)
        throw new IllegalArgumentException("Cannot insert an object into a Store which is already in a store (drIndex=" + obj.getDRIndex() + ")");
    for (T obj : c)
      obj.setDRIndex(items.size());
    return items.addAll(c);
  }

  /**
   * @see Store#add(int, Ann)
   * @throws UnsupportedOperationException
   **/
  @Override
  public boolean addAll(int index, Collection<? extends T> c) {
    throw new UnsupportedOperationException();
  }

  /**
   * Removes all annotation instances from the store, setting their drIndex values to null in the
   * process.
   **/
  @Override
  public void clear() {
    for (T obj : items)
      obj.setDRIndex(null);
    items.clear();
  }

  @Override
  public boolean contains(Object o) {
    return items.contains(o);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return items.containsAll(c);
  }

  /**
   * Creates a new annotation instance, adds it to the store, and returns the instance. This is
   * simply a convenience method to save the user from creating and adding to the store
   * separately.
   **/
  public T create(final Class<T> klass) {
    T obj;
    try {
      obj = (T) klass.newInstance();
    }
    catch (IllegalAccessException e) {
      throw new DocrepException(e);
    }
    catch (InstantiationException e) {
      throw new DocrepException(e);
    }
    add(obj);
    return obj;
  }

  /**
   * Creates n new annotation instances, adding them all to the store.
   *
   * @see Store#create(Class)
   **/
  public void create(final Class<T> klass, final int n) {
    for (int i = 0; i != n; i++)
      create(klass);
  }

  @Override
  public boolean equals(Object o) {
    return items.equals(o);
  }

  @Override
  public T get(int index) {
    return items.get(index);
  }

  @Override
  public int hashCode() {
    return items.hashCode();
  }

  @Override
  public int indexOf(Object o) {
    return items.indexOf(o);
  }

  @Override
  public boolean isEmpty() {
    return items.isEmpty();
  }

  @Override
  public Iterator<T> iterator() {
    return items.iterator();
  }

  @Override
  public int lastIndexOf(Object o) {
    return items.lastIndexOf(o);
  }

  @Override
  public ListIterator<T> listIterator() {
    return items.listIterator();
  }

  @Override
  public ListIterator<T> listIterator(int index) {
    return items.listIterator(index);
  }

  /**
   * This method is unsupported but required by the {@link List} interface.
   * @throws UnsupportedOperationException
   **/
  @Override
  public T remove(int index) {
    throw new UnsupportedOperationException();
  }

  /**
   * This method is unsupported but required by the {@link List} interface.
   * @throws UnsupportedOperationException
   **/
  @Override
  public boolean remove(Object o) {
    throw new UnsupportedOperationException();
  }

  /**
   * This method is unsupported but required by the {@link List} interface.
   * @throws UnsupportedOperationException
   **/
  @Override
  public boolean removeAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  /**
   * This method is unsupported but required by the {@link List} interface.
   * @throws UnsupportedOperationException
   **/
  @Override
  public boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  /**
   * This method is unsupported but required by the {@link List} interface.
   * @throws UnsupportedOperationException
   **/
  @Override
  public T set(int index, T o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int size() {
    return items.size();
  }

  /**
   * This method is unsupported but required by the {@link List} interface.
   * @throws UnsupportedOperationException
   **/
  @Override
  public List<T> subList(int fromIndex, int toIndex) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object[] toArray() {
    return items.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return items.toArray(a);
  }
}
