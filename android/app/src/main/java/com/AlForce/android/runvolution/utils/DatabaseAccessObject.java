package com.AlForce.android.runvolution.utils;

/**
 * Created by iqbal on 17/02/18.
 */

public interface DatabaseAccessObject<T> {
    T query(int position);
    long insert(T item);
    int delete(int id);
    int update(T item);
    long getQueryCount();
}
