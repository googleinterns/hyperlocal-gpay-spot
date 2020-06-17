package com.hyperlocal.server;

import com.github.jasync.sql.db.RowData;

import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class FakeRowData implements RowData {
    HashMap<String, Object> map;
    List<Object> list;
    public FakeRowData(Object... list) {
        this.map = new HashMap<String, Object>();
        this.list = new ArrayList<Object>();
        String columnName = null;
        boolean itemHasColumnValue = false;
        for (Object item : list)
        {
            if(!itemHasColumnValue) columnName = (String) item;
            else
            {
                this.map.put(columnName, item);
                this.list.add(item);
            }
            itemHasColumnValue = !itemHasColumnValue;
        }
        return;
    }

    public Object get(int index) {
        return this.list.get(index);
    }

    @Override
    public boolean add(Object arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void add(int arg0, Object arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean addAll(Collection<? extends Object> arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean addAll(int arg0, Collection<? extends Object> arg1) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean contains(Object arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int indexOf(Object arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Iterator<Object> iterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int lastIndexOf(Object arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public ListIterator<Object> listIterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ListIterator<Object> listIterator(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean remove(Object arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Object remove(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean removeAll(Collection<?> arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Object set(int arg0, Object arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int size() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<Object> subList(int arg0, int arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object[] toArray() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T[] toArray(T[] arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object get(String columnName) {
        return map.get(columnName);
    }

    @Override
    public <T> T getAs(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T getAs(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean getBoolean(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean getBoolean(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Byte getByte(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Byte getByte(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LocalDateTime getDate(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LocalDateTime getDate(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double getDouble(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Double getDouble(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Float getFloat(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Float getFloat(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer getInt(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer getInt(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long getLong(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long getLong(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getString(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getString(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int rowNumber() {
        // TODO Auto-generated method stub
        return 0;
    }
}