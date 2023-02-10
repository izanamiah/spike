package com.prospect.spike.db.dao;

import com.prospect.spike.db.po.Order;

public interface OrderDao {

    void insertOrder(Order order);

    Order queryOrder(String orderNo);

    void updateOrder(Order order);

}
