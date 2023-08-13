package com.rf.ecommerce.Service.Product;


import com.rf.ecommerce.Dto.DtoConvert;
import com.rf.ecommerce.Dto.Product.ProductDto;
import com.rf.ecommerce.Entity.Admin.Admin;
import com.rf.ecommerce.Entity.Order.Order;
import com.rf.ecommerce.Entity.Product.Category;
import com.rf.ecommerce.Entity.Product.Product;
import com.rf.ecommerce.Repository.Product.ProductRepository;
import com.rf.ecommerce.Service.Admin.AdminService;
import com.rf.ecommerce.Service.Order.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service

public class ProductService {
    private final ProductRepository productRepository;
    private final DtoConvert dtoConvert;
    @Autowired
    private AdminService adminService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    @Lazy
    private OrderService orderService;



    @Autowired
    public ProductService(ProductRepository productRepository, DtoConvert dtoConvert) {
        this.productRepository = productRepository;
        this.dtoConvert = dtoConvert;
    }
    public void save(Product product){
        productRepository.save(product);
    }
    public void delete(Long id){
      productRepository.deleteById(id);
    }
    public boolean existsById(Long id){
        return productRepository.existsById(id);
    }
    public Product findById(Long id){
        return productRepository.findById(id).orElseThrow();
    }

    public List<Product> getAllProducts(){
        return  productRepository.findAll();
    }
    public boolean createToProduct(String username,Product product){
        if(!adminService.existsByUsername(username) || !categoryService.existsByName(product.getCategoryName())){
            return false;
        }
        Admin admin=adminService.findByUsername(username);
        product.setAdmin(admin);
        Category category=categoryService.findByName(product.getCategoryName());
        product.setCategory(category);
        save(product);
        return true;
    }
    public boolean deleteToProduct(Long id){
        if(!existsById(id)){
            return false;
        }
        for (Order order : orderService.getAllOrders()){
            if(order.getProduct().getId().equals(id)){
                orderService.delete(order.getId());
            }
        }
        delete(id);

        return true;
    }
    public boolean updateToProduct(Long id,Product product){
        if(!existsById(id)){
            return false;
        }
        Product product1=findById(id);
        product1.setTitle(product.getTitle());
        product1.setDescription(product.getDescription());
        save(product1);
        return  true;
    }
    public List<ProductDto> getToProducts(String username){
        List<Product> productList=new ArrayList<>();
        for(Product product: getAllProducts()){
            if(product.getAdmin().getUsername().equals(username)){
                productList.add(product);
            }
        }
        return  productList.stream().map(x->dtoConvert.productConvert(x)).collect(Collectors.toList());
    }
    public List<ProductDto> getToCategoryProducts(String category){
        List<Product> productList=new ArrayList<>();
        for(Product product : getAllProducts()){
            if(product.getCategory().getName().equals(category)){
                productList.add(product);
            }
        }
        return productList.stream().map(x->dtoConvert.productConvert(x)).collect(Collectors.toList());
    }

}
