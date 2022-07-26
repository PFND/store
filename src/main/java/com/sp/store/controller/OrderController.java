package com.sp.store.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sp.store.common.Result;
import com.sp.store.entity.Book;
import com.sp.store.entity.Order;
import com.sp.store.mapper.BookMapper;
import com.sp.store.mapper.OrderMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

@RestController
@CrossOrigin
@RequestMapping("/order")
public class OrderController {

    @Resource
    private OrderMapper orderMapper;
    @Resource
    private BookMapper bookMapper;
    @PostMapping
    public Result<?> save(@RequestBody Order order) {
        order.setTotal(bookMapper.selectById(order.getBookId()).getPrice());
        orderMapper.insert(order);
        return Result.success();
    }
    @PutMapping
    public Result<?> update(@RequestBody Order order) {
        System.out.println(orderMapper.selectById(order.getId()));
        orderMapper.updateById(order);
        System.out.println(orderMapper.selectById(order.getId()).getState());
        return Result.success();
    }
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        orderMapper.deleteById(id);
        return Result.success();
    }
    @GetMapping
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNumber, @RequestParam(defaultValue = "10") Integer pageSize, @RequestParam(defaultValue = "") String search) {
        LambdaQueryWrapper<Order> wrapper = Wrappers.lambdaQuery();
        if (search != null && search.length() > 0) {
            wrapper.like(Order::getId, search).or().like(Order::getBookId,search).or().like(Order::getUserId,search);
        }
        wrapper.orderByDesc(Order::getCreateTime);
        //wrapper.orderByAsc(Order::getId);
        Page<Order> orderPage=orderMapper.selectPage(new Page<>(pageNumber, pageSize), wrapper);
        return Result.success(orderPage);
    }
    @GetMapping("/createorder")
    public Result<?> createOrder(@RequestParam Integer userId, @RequestParam Integer bookId){
        Order order=new Order();
        order.setUserId(userId);
        order.setBookId(bookId);
        order.setTotal(bookMapper.selectById(order.getBookId()).getPrice());
        orderMapper.insert(order);
        return Result.success();
    }
    @GetMapping("/cancelorder")
    public Result<?> cancelOrder(@RequestParam Long orderId){
        Order order=orderMapper.selectById(orderId);
        order.setState((short) 5);
        orderMapper.updateById(order);
        return Result.success();
    }
    @GetMapping("/myorder/{userId}")
    public Result<?> myOrder( @PathVariable Integer userId) {
        QueryWrapper<Order> wrapper=Wrappers.query();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("create_time");
        List<Order> list=orderMapper.selectList(wrapper);
        List<Book> books= new ArrayList<>();
        for(Order order:list){
            QueryWrapper<Book> bwrapper=Wrappers.query();
            bwrapper.eq("id", order.getBookId());
            Book book=bookMapper.selectOne(bwrapper);
            books.add(book);
        }
        Vector v=new Vector<>();
        v.add(list);
        v.add(books);
        return Result.success(v);
    }
}
