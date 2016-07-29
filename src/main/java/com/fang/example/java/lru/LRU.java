package com.fang.example.java.lru;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Created by andy on 7/26/16.
 */
public class LRU {

    /** Cached object hashtable */
    Hashtable _hash = new Hashtable();

    /**
     * Maximum number of objects in the cache.
     */
    int _max;

    /**
     * Beginning of linked-list of cache elements.  First entry is element
     * which has been used least recently.
     */
    CacheEntry _first;

    /**
     * End of linked-list of cache elements.  Last entry is element
     * which has been used most recently.
     */
    CacheEntry _last;


    /**
     * Cache eviction listeners
     */
    Vector listeners = new Vector();


    /**
     * Construct an MRU with a given maximum number of objects.
     */
    public LRU(int max) {
        if (max <= 0) {
            throw new IllegalArgumentException("MRU cache must contain at least one entry");
        }
        _max = max;
    }


    /**
     * Place an object in the cache.
     */
    public void put(Object key, Object value) throws Exception {
        CacheEntry entry = (CacheEntry)_hash.get(key);
        if (entry != null) {
            entry.setValue(value);
            touchEntry(entry);
        } else {

            if (_hash.size() == _max) {
                // purge and recycle entry
                entry = purgeEntry();
                entry.setKey(key);
                entry.setValue(value);
            } else {
                entry = new CacheEntry(key, value);
            }
            addEntry(entry);
            _hash.put(entry.getKey(), entry);
        }
    }


    /**
     * Obtain an object in the cache
     */
    public Object get(Object key) {
        CacheEntry entry = (CacheEntry)_hash.get(key);
        if (entry != null) {
            touchEntry(entry);
            return entry.getValue();
        } else {
            return null;
        }
    }


    /**
     * Remove an object from the cache
     */
    public void remove(Object key) {
        CacheEntry entry = (CacheEntry)_hash.get(key);
        if (entry != null) {
            removeEntry(entry);
            _hash.remove(entry.getKey());
        }
    }


    /**
     * Remove all objects from the cache
     */
    public void removeAll() {
        _hash = new Hashtable();
        _first = null;
        _last = null;
    }


    /**
     * Enumerate elements' values in the cache
     */
    public Enumeration elements() {
        return new MRUEnumeration(_hash.elements());
    }

    /**
     * Add a listener to this cache policy
     *
     * @param listener Listener to add to this policy
     */
    public void addListener(CachePolicyListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Cannot add null listener.");
        }
        if ( ! listeners.contains(listener)) {
            listeners.addElement(listener);
        }
    }

    /**
     * Remove a listener from this cache policy
     *
     * @param listener Listener to remove from this policy
     */
    public void removeListener(CachePolicyListener listener) {
        listeners.removeElement(listener);
    }

    /**
     * Add a CacheEntry.  Entry goes at the end of the list.
     */
    protected void addEntry(CacheEntry entry) {
        if (_first == null) {
            _first = entry;
            _last = entry;
        } else {
            _last.setNext(entry);
            entry.setPrevious(_last);
            _last = entry;
        }
    }


    /**
     * Remove a CacheEntry from linked list
     */
    protected void removeEntry(CacheEntry entry) {
        if (entry == _first) {
            _first = entry.getNext();
        }
        if (_last == entry) {
            _last = entry.getPrevious();
        }
        CacheEntry previous = entry.getPrevious();
        CacheEntry next = entry.getNext();
        if (previous != null) {
            previous.setNext(next);
        }
        if (next != null) {
            next.setPrevious(previous);
        }
        entry.setPrevious(null);
        entry.setNext(null);
    }

    /**
     * Place entry at the end of linked list -- Most Recently Used
     */
    protected void touchEntry(CacheEntry entry) {
        if (_last == entry) {
            return;
        }
        removeEntry(entry);
        addEntry(entry);
    }

    /**
     * Purge least recently used object from the cache
     *
     * @return recyclable CacheEntry
     */
    protected CacheEntry purgeEntry() throws Exception {
        CacheEntry entry = _first;

        // Notify policy listeners first. if any of them throw an
        // eviction exception, then the internal data structure
        // remains untouched.
        CachePolicyListener listener;
        for (int i=0; i<listeners.size(); i++) {
            listener = (CachePolicyListener)listeners.elementAt(i);
            listener.cacheObjectEvicted(entry.getValue());
        }

        removeEntry(entry);
        _hash.remove(entry.getKey());

        entry.setValue(null);
        return entry;
    }

}

/**
 * State information for cache entries.
 */
class CacheEntry {
    private Object _key;
    private Object _value;

    private CacheEntry _previous;
    private CacheEntry _next;

    CacheEntry(Object key, Object value) {
        _key = key;
        _value = value;
    }

    Object getKey() {
        return _key;
    }

    void setKey(Object obj) {
        _key = obj;
    }

    Object getValue() {
        return _value;
    }

    void setValue(Object obj) {
        _value = obj;
    }

    CacheEntry getPrevious() {
        return _previous;
    }

    void setPrevious(CacheEntry entry) {
        _previous = entry;
    }

    CacheEntry getNext() {
        return _next;
    }

    void setNext(CacheEntry entry) {
        _next = entry;
    }
}

/**
 * Enumeration wrapper to return actual user objects instead of
 * CacheEntries.
 */
class MRUEnumeration implements Enumeration {
    Enumeration _enum;

    MRUEnumeration(Enumeration enume) {
        _enum = enume;
    }

    public boolean hasMoreElements() {
        return _enum.hasMoreElements();
    }

    public Object nextElement() {
        CacheEntry entry = (CacheEntry)_enum.nextElement();
        return entry.getValue();
    }
}

