package com.example.eStore.service;

import com.example.eStore.dto.BaseResultDTO;
import com.example.eStore.dto.constants.Constants;
import com.example.eStore.dto.request.AddToCartRequest;
import com.example.eStore.dto.response.ApiResponseFactory;
import com.example.eStore.dto.response.CartItemResponse;
import com.example.eStore.dto.response.CartResponse;
import com.example.eStore.entity.Cart;
import com.example.eStore.entity.CartItem;
import com.example.eStore.entity.Product;
import com.example.eStore.exception.AppException;
import com.example.eStore.repository.CartItemRepository;
import com.example.eStore.repository.CartRepository;
import com.example.eStore.repository.ProductRepository;
import com.example.eStore.repository.UserRepository;
import com.example.eStore.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public BaseResultDTO<Void> addToCart(AddToCartRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();

        Cart cart = getOrCreateCart(userId);

        Product product = productRepository
                .findById(request.getProductId())
                .orElseThrow(() -> new AppException(
                        "Product not found",
                        Constants.ErrorCode.Cart.ADD_PRODUCT_NOT_FOUND
                ));

        Optional<CartItem> optionalItem =
                cartItemRepository.findByCartIdAndProductId(
                        cart.getId(),
                        product.getId());

        CartItem item;

        if (optionalItem.isPresent()) {
            item = optionalItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
        } else {
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(request.getQuantity());
        }

        cartItemRepository.save(item);

        recalculateCart(cart);
        return ApiResponseFactory.success(Constants.Message.Cart.ADD_SUCCESS);
    }

    public BaseResultDTO<Void> updateCartItem(Long itemId, Integer quantity) {
        Long userId = SecurityUtils.getCurrentUserId();

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new AppException(
                        "Cart item not found",
                        Constants.ErrorCode.Cart.UPDATE_ITEM_NOT_FOUND
                ));

        assertCartItemOwnedByUser(item, userId);

        item.setQuantity(quantity);

        cartItemRepository.save(item);

        recalculateCart(item.getCart());
        return ApiResponseFactory.success(Constants.Message.Cart.UPDATE_SUCCESS);
    }

    public BaseResultDTO<Void> removeItem(Long itemId) {
        Long userId = SecurityUtils.getCurrentUserId();

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new AppException(
                        "Cart item not found",
                        Constants.ErrorCode.Cart.REMOVE_ITEM_NOT_FOUND
                ));

        assertCartItemOwnedByUser(item, userId);

        Cart cart = item.getCart();

        cartItemRepository.delete(item);

        recalculateCart(cart);
        return ApiResponseFactory.success(Constants.Message.Cart.REMOVE_SUCCESS);
    }

    @Transactional
    public BaseResultDTO<Void> clearCart() {
        Long userId = SecurityUtils.getCurrentUserId();
        Optional<Cart> cartOptional = cartRepository.findByUserId(userId);

        if (cartOptional.isEmpty()) {
            return ApiResponseFactory.success(Constants.Message.Cart.CLEAR_SUCCESS);
        }

        Cart cart = cartOptional.get();
        cartItemRepository.deleteAllByCartId(cart.getId());
        cart.setTotalPrice(0L);
        cartRepository.save(cart);

        return ApiResponseFactory.success(Constants.Message.Cart.CLEAR_SUCCESS);
    }

    public BaseResultDTO<CartResponse> getCart() {
        Long userId = SecurityUtils.getCurrentUserId();

        Cart cart = getOrCreateCart(userId);

        List<CartItem> items =
                cartItemRepository.findByCartId(cart.getId());

        List<CartItemResponse> itemResponses = items.stream()
                .map(item -> CartItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .productPrice(item.getProduct().getPrice())
                        .quantity(item.getQuantity())
                        .productImageUrl(item.getProduct().getThumbnailUrl())
                        .build())
                .toList();

        CartResponse response = CartResponse.builder()
                .id(cart.getId())
                .userId(userId)
                .totalPrice(cart.getTotalPrice())
                .items(itemResponses)
                .build();

        return ApiResponseFactory.success(Constants.Message.Cart.GET_CART_SUCCESS, response);
    }

    private void assertCartItemOwnedByUser(CartItem item, Long userId) {
        Long ownerId = item.getCart().getUser().getId();
        if (!ownerId.equals(userId)) {
            throw new AppException(
                    "Cart item does not belong to current user",
                    Constants.ErrorCode.Cart.ITEM_NOT_OWNED);
        }
    }

    private Cart getOrCreateCart(Long userId) {

        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(userRepository.findById(userId)
                            .orElseThrow(() -> new AppException(
                                    "User not found",
                                    Constants.ErrorCode.Cart.ADD_USER_NOT_FOUND
                            )));
                    cart.setTotalPrice(0L);
                    return cartRepository.save(cart);
                });
    }

    private void recalculateCart(Cart cart) {

        List<CartItem> items =
                cartItemRepository.findByCartId(cart.getId());

        long total = 0;

        for (CartItem item : items) {
            total += item.getQuantity() *
                    item.getProduct().getPrice();
        }

        cart.setTotalPrice(total);

        cartRepository.save(cart);
    }
}
