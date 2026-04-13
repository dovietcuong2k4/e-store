package com.example.eStore.dto.constants;

public interface Constants {
    interface OrderStatus {
        String CREATED = "CREATED";
        String CONFIRMED = "CONFIRMED";
        String PREPARING = "PREPARING";
        String READY_FOR_SHIPPING = "READY_FOR_SHIPPING";
        String SHIPPING = "SHIPPING";
        String DELIVERED = "DELIVERED";
        String DELIVERY_FAILED = "DELIVERY_FAILED";
        String CANCELLED = "CANCELLED";
    }

    interface Role {
        String ADMIN = "ADMIN";
        String STAFF = "STAFF";
        String SHIPPER = "SHIPPER";
        String CUSTOMER = "CUSTOMER";
    }

    interface ErrorCode {
        interface User {
            String REGISTER_EMAIL_EXISTS = "USER_REGISTER_EMAIL_EXISTS";
            String REGISTER_ROLE_NOT_FOUND = "USER_REGISTER_ROLE_NOT_FOUND";
            String LOGIN_EMAIL_NOT_FOUND = "USER_LOGIN_EMAIL_NOT_FOUND";
            String LOGIN_INVALID_PASSWORD = "USER_LOGIN_INVALID_PASSWORD";
            String GET_TOTAL_FAILED = "USER_GET_TOTAL_FAILED";
        }

        interface Cart {
            String ADD_PRODUCT_NOT_FOUND = "CART_ADD_PRODUCT_NOT_FOUND";
            String ADD_USER_NOT_FOUND = "CART_ADD_USER_NOT_FOUND";
            String UPDATE_ITEM_NOT_FOUND = "CART_UPDATE_ITEM_NOT_FOUND";
            String REMOVE_ITEM_NOT_FOUND = "CART_REMOVE_ITEM_NOT_FOUND";
        }

        interface Order {
            String CREATE_CART_NOT_FOUND = "ORDER_CREATE_CART_NOT_FOUND";
            String CREATE_EMPTY_CART = "ORDER_CREATE_EMPTY_CART";
            String CREATE_USER_NOT_FOUND = "ORDER_CREATE_USER_NOT_FOUND";
            String UPDATE_NOT_FOUND = "ORDER_UPDATE_NOT_FOUND";
            String UPDATE_INVALID_STATUS = "ORDER_UPDATE_INVALID_STATUS";
            String GET_BY_USER_FAILED = "ORDER_GET_BY_USER_FAILED";
        }

        interface Product {
            String CREATE_CATEGORY_NOT_FOUND = "PRODUCT_CREATE_CATEGORY_NOT_FOUND";
            String CREATE_BRAND_NOT_FOUND = "PRODUCT_CREATE_BRAND_NOT_FOUND";
            String UPDATE_NOT_FOUND = "PRODUCT_UPDATE_NOT_FOUND";
            String DELETE_NOT_FOUND = "PRODUCT_DELETE_NOT_FOUND";
        }
    }

    interface Message {
        interface Auth {
            String REGISTER_SUCCESS = "Register success";
            String LOGIN_SUCCESS = "Login success";
        }

        interface Cart {
            String ADD_SUCCESS = "Add to cart successfully";
            String UPDATE_SUCCESS = "Update cart item successfully";
            String REMOVE_SUCCESS = "Remove cart item successfully";
            String GET_CART_SUCCESS = "Get cart successfully";
        }

        interface Order {
            String CREATE_SUCCESS = "Create order successfully";
            String GET_SUCCESS = "Get orders successfully";
            String CONFIRM_SUCCESS = "Order confirmed successfully";
            String PREPARING_SUCCESS = "Order is being prepared";
            String READY_SUCCESS = "Order is ready for shipping";
            String SHIPPING_SUCCESS = "Order is now shipping";
            String DELIVERY_SUCCESS = "Order delivered successfully";
            String DELIVERY_FAILED = "Delivery failed";
            String CANCEL_SUCCESS = "Order cancelled successfully";
        }

        interface Product {
            String CREATE_SUCCESS = "Create product successfully";
            String GET_SUCCESS = "Get products successfully";
            String UPDATE_SUCCESS = "Update product successfully";
            String DELETE_SUCCESS = "Delete product successfully";
        }

        interface User {
            String GET_TOTAL_SUCCESS = "Get total users successfully";
        }
    }
}
