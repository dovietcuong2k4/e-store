package com.example.eStore.service;

import com.example.eStore.dto.request.AddToCartRequest;
import com.example.eStore.entity.Cart;
import com.example.eStore.entity.CartItem;
import com.example.eStore.entity.Product;
import com.example.eStore.repository.CartItemRepository;
import com.example.eStore.repository.CartRepository;
import com.example.eStore.repository.ProductRepository;
import com.example.eStore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public void addToCart(Long userId, AddToCartRequest request) {

        Cart cart = getOrCreateCart(userId);

        Product product = productRepository
                .findById(request.getProductId())
                .orElseThrow();

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
    }

    public void updateCartItem(Long itemId, Integer quantity) {

        CartItem item = cartItemRepository.findById(itemId).orElseThrow();

        item.setQuantity(quantity);

        cartItemRepository.save(item);

        recalculateCart(item.getCart());
    }

    public void removeItem(Long itemId) {

        CartItem item = cartItemRepository.findById(itemId).orElseThrow();

        Cart cart = item.getCart();

        cartItemRepository.delete(item);

        recalculateCart(cart);
    }



    private Cart getOrCreateCart(Long userId) {

        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(userRepository.findById(userId).orElseThrow());
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
