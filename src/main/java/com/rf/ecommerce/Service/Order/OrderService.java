package com.rf.ecommerce.Service.Order;

import com.rf.ecommerce.Dto.DtoConvert;
import com.rf.ecommerce.Dto.Order.OrderDto;
import com.rf.ecommerce.Dto.Order.UpdateStatusDto;
import com.rf.ecommerce.Entity.Order.Order;
import com.rf.ecommerce.Entity.Product.Product;
import com.rf.ecommerce.Entity.User.User;
import com.rf.ecommerce.Repository.Order.OrderRepository;
import com.rf.ecommerce.Repository.Product.ProductRepository;
import com.rf.ecommerce.Service.Product.ProductService;
import com.rf.ecommerce.Service.User.UserService;
import com.rf.ecommerce.error.ApiError;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    @Lazy
    private final ProductService productService;
    private final UserService userService;
    private final DtoConvert dtoConvert;
    public void save(Order order){orderRepository.save(order);}
    public void delete(Long orderId){orderRepository.deleteById(orderId);}
    public List<Order> getAllOrders(){return orderRepository.findAll();}
    public ResponseEntity<?> createAtOrder(Order order,Long productId,String email){
        if(!productService.existsById(productId) || !userService.existsByEmail(email)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(sendApiError());
        }
        Product product=productService.findById(productId);
        order.setProduct(product);
        User user=userService.findByEmail(email);
        order.setUser(user);
        order.setOrderStatus("Sipariş oluşturuldu");
        save(order);
        return ResponseEntity.ok().body("Sipariş oluşturuldu");
    }
    public ResponseEntity<?> deleteAtOrder(Long id){
      if(!orderRepository.existsById(id)){
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(sendApiError());
      }
      delete(id);
        return ResponseEntity.ok().body("Sipariş İptal Edildi");
    }
    public ResponseEntity<?> updateStatus(Long orderId, UpdateStatusDto order){
        if(!orderRepository.existsById(orderId)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(sendApiError());
        }
        Order updateOrder=orderRepository.findById(orderId).orElseThrow();
        updateOrder.setOrderStatus(order.getOrderStatus());
        save(updateOrder);
        return ResponseEntity.ok().body("Status Güncellendi");
    }
    public List<OrderDto> getList(String username){
        List<Order> newList=new ArrayList<>();
        for(Order order : getAllOrders() ){
            if(order.getProduct().getAdmin().getUsername().equals(username)){
                newList.add(order);
            }
        }
        return newList.stream().map(x->dtoConvert.orderConvert(x)).collect(Collectors.toList());
    }
    private ApiError sendApiError(){
        return new ApiError(404,"Sipariş bulunamadi","api/order");
    }



}
