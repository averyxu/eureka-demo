package com.averyxu.servcie.impl;

import com.averyxu.pojo.Order;
import com.averyxu.pojo.Product;
import com.averyxu.servcie.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final RestTemplate restTemplate;
    private final DiscoveryClient discoveryClient;

    @Autowired
    public OrderServiceImpl(RestTemplate restTemplate, DiscoveryClient discoveryClient) {
        this.restTemplate = restTemplate;
        this.discoveryClient = discoveryClient;
    }

    @Override
    public Order selectOrderById(Integer id) {
        return new Order(id, "order-001", "中国", 31994D,
                selectProductListByDiscoveryClient());
    }

    private List<Product> selectProductListByDiscoveryClient() {
        StringBuffer sb = null;

        List<String> serviceIds = discoveryClient.getServices();
        if (CollectionUtils.isEmpty(serviceIds))
            return null;

        List<ServiceInstance> serviceInstances = discoveryClient.getInstances("service-provider");
        if (CollectionUtils.isEmpty(serviceInstances))
            return null;

        ServiceInstance si = serviceInstances.get(0);
        sb = new StringBuffer();
        sb.append("http://" + si.getHost() + ":" + si.getPort() + "/product/list");

        ResponseEntity<List<Product>> response = restTemplate.exchange(
                sb.toString(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Product>>() {});

        return response.getBody();
    }
}
