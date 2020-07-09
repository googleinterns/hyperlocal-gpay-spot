package com.hyperlocal.server;

import com.github.jasync.sql.db.ResultSet;
import com.github.jasync.sql.db.RowData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class FakeResultSet implements ResultSet {
    ArrayList<RowData> list;

    public FakeResultSet(RowData... list) {
        this.list = new ArrayList<RowData>();
        for (RowData item : list)
            this.list.add(item);
    }

    public RowData get(int index) {
        return list.get(index);
    }

    @Override
    public boolean add(RowData arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void add(int arg0, RowData arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean addAll(Collection<? extends RowData> arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean addAll(int arg0, Collection<? extends RowData> arg1) {
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
    public Iterator<RowData> iterator() {
        return this.list.iterator();
    }

    @Override
    public int lastIndexOf(Object arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public ListIterator<RowData> listIterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ListIterator<RowData> listIterator(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean remove(Object arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public RowData remove(int arg0) {
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
    public RowData set(int arg0, RowData arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int size() {
        // TODO Auto-generated method stub
        return this.list.size();
    }

    @Override
    public List<RowData> subList(int arg0, int arg1) {
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
    public List<String> columnNames() {
        // TODO Auto-generated method stub
        return null;
    }
}