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

    interface VoucherStatus {
        String AVAILABLE = "AVAILABLE";
        String USED = "USED";
        String EXPIRED = "EXPIRED";
    }

    interface DiscountType {
        String PERCENTAGE = "PERCENTAGE";
        String FIXED = "FIXED";
    }

    interface VoucherAction {
        String USED = "USED";
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

        interface Profile {
            String NOT_FOUND = "PROFILE_NOT_FOUND";
            String FIELD_NOT_SUPPORTED = "PROFILE_FIELD_NOT_SUPPORTED";
            String FIELD_NOT_EDITABLE = "PROFILE_FIELD_NOT_EDITABLE";
            String INVALID_FULL_NAME = "PROFILE_INVALID_FULL_NAME";
            String INVALID_PHONE = "PROFILE_INVALID_PHONE";
            String INVALID_ADDRESS = "PROFILE_INVALID_ADDRESS";
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

        interface Voucher {
            String TEMPLATE_NOT_FOUND = "VOUCHER_TEMPLATE_NOT_FOUND";
            String CODE_EXISTS = "VOUCHER_CODE_EXISTS";
            String USER_NOT_FOUND = "VOUCHER_USER_NOT_FOUND";
            String USER_VOUCHER_NOT_FOUND = "VOUCHER_USER_VOUCHER_NOT_FOUND";
            String NOT_OWNED = "VOUCHER_NOT_OWNED";
            String NOT_AVAILABLE = "VOUCHER_NOT_AVAILABLE";
            String EXPIRED = "VOUCHER_EXPIRED";
            String INACTIVE = "VOUCHER_INACTIVE";
            String MIN_VALUE_NOT_MET = "VOUCHER_MIN_VALUE_NOT_MET";
            String START_DATE_NOT_REACHED = "VOUCHER_START_DATE_NOT_REACHED";
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

        interface Review {
            String PRODUCT_NOT_FOUND = "REVIEW_PRODUCT_NOT_FOUND";
            String NOT_PURCHASED = "REVIEW_NOT_PURCHASED";
            String ALREADY_REVIEWED = "REVIEW_ALREADY_REVIEWED";
            String NOT_FOUND = "REVIEW_NOT_FOUND";
            String NOT_OWNER = "REVIEW_NOT_OWNER";
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

        interface Voucher {
            String CREATE_SUCCESS = "Create voucher template successfully";
            String ASSIGN_SUCCESS = "Assign voucher to user successfully";
            String APPLY_SUCCESS = "Apply voucher successfully";
            String GET_SUCCESS = "Get vouchers successfully";
        }

        interface Review {
            String CREATE_SUCCESS = "Review submitted successfully";
            String UPDATE_SUCCESS = "Review updated successfully";
            String DELETE_SUCCESS = "Review deleted successfully";
            String GET_SUCCESS = "Get reviews successfully";
        }

        interface Contact {
            String SEND_SUCCESS = "Contact sent successfully";
            String GET_ALL_CONTACTS_SUCCESS = "Get all contacts successfully";
        }

        interface User {
            String GET_TOTAL_SUCCESS = "Get total users successfully";
            String ADMIN_GET_ALL_SUCCESS = "Get users successfully";
            String ADMIN_GET_DETAIL_SUCCESS = "Get user detail successfully";
            String ADMIN_CREATE_SUCCESS = "Create user successfully";
            String ADMIN_UPDATE_SUCCESS = "Update user successfully";
            String ADMIN_DELETE_SUCCESS = "Delete user successfully";
        }

        interface Profile {
            String GET_SUCCESS = "Get profile successfully";
            String UPDATE_SUCCESS = "Update profile successfully";
        }
    }
}
