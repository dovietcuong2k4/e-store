package com.example.eStore.dto.constants;

public interface Constants {
    interface OrderStatus {
        String CREATED = "CREATED";
        String PROCESSING = "PROCESSING";
        String READY_FOR_SHIPPING = "READY_FOR_SHIPPING";
        String SHIPPING = "SHIPPING";
        String DELIVERED = "DELIVERED";
        String DELIVERY_FAILED = "DELIVERY_FAILED";
        String CANCELLED = "CANCELLED";
    }

    interface Role {
        String ADMIN = "ROLE_ADMIN";
        String STAFF = "ROLE_STAFF";
        String SHIPPER = "ROLE_SHIPPER";
        String CUSTOMER = "ROLE_CUSTOMER";
    }

    interface ErrorCode {
        interface User {
            String REGISTER_EMAIL_EXISTS = "USER_REGISTER_EMAIL_EXISTS";
            String REGISTER_ROLE_NOT_FOUND = "USER_REGISTER_ROLE_NOT_FOUND";
            String LOGIN_EMAIL_NOT_FOUND = "USER_LOGIN_EMAIL_NOT_FOUND";
            String LOGIN_INVALID_PASSWORD = "USER_LOGIN_INVALID_PASSWORD";
            String GET_TOTAL_FAILED = "USER_GET_TOTAL_FAILED";
            String ADMIN_GET_NOT_FOUND = "USER_ADMIN_GET_NOT_FOUND";
            String ADMIN_CREATE_EMAIL_EXISTS = "USER_ADMIN_CREATE_EMAIL_EXISTS";
            String ADMIN_CREATE_ROLE_NOT_FOUND = "USER_ADMIN_CREATE_ROLE_NOT_FOUND";
            String ADMIN_UPDATE_ROLE_NOT_FOUND = "USER_ADMIN_UPDATE_ROLE_NOT_FOUND";
            String ADMIN_UPDATE_EMAIL_EXISTS = "USER_ADMIN_UPDATE_EMAIL_EXISTS";
            String ADMIN_DELETE_NOT_FOUND = "USER_ADMIN_DELETE_NOT_FOUND";
        }

        interface Security {
            String UNAUTHENTICATED = "SECURITY_UNAUTHENTICATED";
        }

        interface Cart {
            String ADD_PRODUCT_NOT_FOUND = "CART_ADD_PRODUCT_NOT_FOUND";
            String ADD_USER_NOT_FOUND = "CART_ADD_USER_NOT_FOUND";
            String UPDATE_ITEM_NOT_FOUND = "CART_UPDATE_ITEM_NOT_FOUND";
            String REMOVE_ITEM_NOT_FOUND = "CART_REMOVE_ITEM_NOT_FOUND";
            String ITEM_NOT_OWNED = "CART_ITEM_NOT_OWNED";
        }

        interface Order {
            String CREATE_CART_NOT_FOUND = "ORDER_CREATE_CART_NOT_FOUND";
            String CREATE_EMPTY_CART = "ORDER_CREATE_EMPTY_CART";
            String CREATE_USER_NOT_FOUND = "ORDER_CREATE_USER_NOT_FOUND";
            String CREATE_OUT_OF_STOCK = "ORDER_CREATE_OUT_OF_STOCK";
            String UPDATE_NOT_FOUND = "ORDER_UPDATE_NOT_FOUND";
            String UPDATE_INVALID_STATUS = "ORDER_UPDATE_INVALID_STATUS";
            String CANCEL_NOT_ALLOWED = "ORDER_CANCEL_NOT_ALLOWED";
            String GET_BY_USER_FAILED = "ORDER_GET_BY_USER_FAILED";
            String ASSIGN_SHIPPER_INVALID = "ORDER_ASSIGN_SHIPPER_INVALID";
        }

        interface Product {
            String CREATE_CATEGORY_NOT_FOUND = "PRODUCT_CREATE_CATEGORY_NOT_FOUND";
            String CREATE_BRAND_NOT_FOUND = "PRODUCT_CREATE_BRAND_NOT_FOUND";
            String GET_DETAIL_NOT_FOUND = "PRODUCT_GET_DETAIL_NOT_FOUND";
            String UPDATE_NOT_FOUND = "PRODUCT_UPDATE_NOT_FOUND";
            String UPDATE_CATEGORY_NOT_FOUND = "PRODUCT_UPDATE_CATEGORY_NOT_FOUND";
            String UPDATE_BRAND_NOT_FOUND = "PRODUCT_UPDATE_BRAND_NOT_FOUND";
            String DELETE_NOT_FOUND = "PRODUCT_DELETE_NOT_FOUND";
        }

        interface Contact {
            String SEND_MAIL_FAILED = "CONTACT_SEND_MAIL_FAILED";
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
            String CLEAR_SUCCESS = "Clear cart successfully";
            String GET_CART_SUCCESS = "Get cart successfully";
        }

        interface Order {
            String CREATE_SUCCESS = "Create order successfully";
            String GET_SUCCESS = "Get orders successfully";
            String PROCESSING_SUCCESS = "Order is being processed";
            String READY_SUCCESS = "Order is ready for shipping";
            String SHIPPING_SUCCESS = "Order is being shipped";
            String DELIVERY_SUCCESS = "Order delivered successfully";
            String DELIVERY_FAILED = "Delivery failed";
            String CANCEL_SUCCESS = "Order cancelled successfully";
            String ASSIGN_SHIPPER_SUCCESS = "Shipper assigned successfully";
        }

        interface Product {
            String CREATE_SUCCESS = "Create product successfully";
            String GET_SUCCESS = "Get products successfully";
            String GET_DETAIL_SUCCESS = "Get product detail successfully";
            String UPDATE_SUCCESS = "Update product successfully";
            String DELETE_SUCCESS = "Delete product successfully";
        }

        interface Contact {
            String SEND_SUCCESS = "Contact sent successfully";
        }

        interface User {
            String GET_TOTAL_SUCCESS = "Get total users successfully";
            String ADMIN_GET_ALL_SUCCESS = "Get users successfully";
            String ADMIN_GET_DETAIL_SUCCESS = "Get user detail successfully";
            String ADMIN_CREATE_SUCCESS = "Create user successfully";
            String ADMIN_UPDATE_SUCCESS = "Update user successfully";
            String ADMIN_DELETE_SUCCESS = "Delete user successfully";
        }
    }
}
