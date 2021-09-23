package dev.thomazz.pledge.util.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

@SuppressWarnings({"unchecked"})
public abstract class SynchronizedListWrapper<T> extends ListWrapper<T> {
    public SynchronizedListWrapper(List<T> base) {
        super(base);
    }

    public abstract void onAdd(T t);

    @Override
    public int size() {
        synchronized (this) {
            return this.base.size();
        }
    }

    @Override
    public boolean isEmpty() {
        synchronized (this) {
            return this.base.isEmpty();
        }
    }

    @Override
    public boolean contains(Object o) {
        synchronized (this) {
            return this.base.contains(o);
        }
    }

    @Override
    public Iterator<T> iterator() {
        synchronized (this) {
            return listIterator();
        }
    }

    @Override
    public Object[] toArray() {
        synchronized (this) {
            return this.base.toArray();
        }
    }

    @Override
    public boolean add(T o) {
        this.onAdd(o);
        synchronized (this) {
            return this.base.add(o);
        }
    }

    @Override
    public boolean remove(Object o) {
        synchronized (this) {
            return this.base.remove(o);
        }
    }

    @Override
    public boolean addAll(Collection c) {
        for (Object o : c) {
            this.onAdd((T) o);
        }
        synchronized (this) {
            return this.base.addAll(c);
        }
    }

    @Override
    public boolean addAll(int index, Collection c) {
        for (Object o : c) {
            this.onAdd((T) o);
        }
        synchronized (this) {
            return this.base.addAll(index, c);
        }
    }

    @Override
    public void clear() {
        synchronized (this) {
            this.base.clear();
        }
    }

    @Override
    public T get(int index) {
        synchronized (this) {
            return this.base.get(index);
        }
    }

    @Override
    public T set(int index, T element) {
        synchronized (this) {
            return this.base.set(index, element);
        }
    }

    @Override
    public void add(int index, T element) {
        synchronized (this) {
            this.base.add(index, element);
        }
    }

    @Override
    public T remove(int index) {
        synchronized (this) {
            return this.base.remove(index);
        }
    }

    @Override
    public int indexOf(Object o) {
        synchronized (this) {
            return this.base.indexOf(o);
        }
    }

    @Override
    public int lastIndexOf(Object o) {
        synchronized (this) {
            return this.base.lastIndexOf(o);
        }
    }

    @Override
    public ListIterator<T> listIterator() {
        synchronized (this) {
            return this.base.listIterator();
        }
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        synchronized (this) {
            return this.base.listIterator(index);
        }
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        synchronized (this) {
            return this.base.subList(fromIndex, toIndex);
        }
    }

    @Override
    public boolean retainAll(Collection c) {
        synchronized (this) {
            return this.base.retainAll(c);
        }
    }

    @Override
    public boolean removeAll(Collection c) {
        synchronized (this) {
            return this.base.removeAll(c);
        }
    }

    @Override
    public boolean containsAll(Collection c) {
        synchronized (this) {
            return this.base.containsAll(c);
        }
    }

    @Override
    public Object[] toArray(Object[] a) {
        synchronized (this) {
            return this.base.toArray(a);
        }
    }
}
