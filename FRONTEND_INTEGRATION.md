// FRONTEND INTEGRATION EXAMPLES

// =====================================================
// 1. REACT EXAMPLE
// =====================================================

// ProductDetail.jsx
import React, { useState, useEffect } from 'react';

const ProductDetail = ({ productId }) => {
  const [product, setProduct] = useState(null);
  const [recommendations, setRecommendations] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    // Lấy thông tin sản phẩm
    fetchProduct();
    // Lấy gợi ý sản phẩm liên quan
    fetchRecommendations();
  }, [productId]);

  const fetchProduct = async () => {
    try {
      const response = await fetch(`/api/products/detail/${productId}`);
      const data = await response.json();
      setProduct(data.data);
    } catch (err) {
      setError('Failed to load product');
    }
  };

  const fetchRecommendations = async () => {
    setLoading(true);
    try {
      const response = await fetch(`/api/products/${productId}/recommendations`);
      const data = await response.json();
      setRecommendations(data.recommendations);
    } catch (err) {
      console.error('Failed to load recommendations:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="product-detail">
      {product && (
        <div className="product-info">
          <h1>{product.name}</h1>
          <p>Giá: {product.price.toLocaleString()}đ</p>
          <p>CPU: {product.cpu}</p>
          <p>RAM: {product.ram}</p>
        </div>
      )}

      {/* AI Recommendations Section */}
      <div className="recommendations-section">
        <h2>🤖 Sản phẩm được AI gợi ý cho bạn</h2>
        
        {loading && <p>Đang tải gợi ý...</p>}
        
        {error && <p className="error">{error}</p>}
        
        {recommendations.length > 0 && (
          <div className="recommendations-grid">
            {recommendations.map((rec) => (
              <div key={rec.productId} className="recommendation-card">
                {rec.thumbnailUrl && (
                  <img src={rec.thumbnailUrl} alt={rec.productName} />
                )}
                <h3>{rec.productName}</h3>
                <p className="price">{rec.price.toLocaleString()}đ</p>
                <p className="reason">💡 {rec.reason}</p>
                <button onClick={() => navigateToProduct(rec.productId)}>
                  Xem chi tiết
                </button>
              </div>
            ))}
          </div>
        )}

        {!loading && recommendations.length === 0 && (
          <p>Không có sản phẩm gợi ý nào</p>
        )}
      </div>
    </div>
  );
};

// =====================================================
// 2. VANILLA JAVASCRIPT EXAMPLE
// =====================================================

class ProductDetailPage {
  constructor(productId) {
    this.productId = productId;
    this.recommendationsContainer = document.getElementById('recommendations');
    this.init();
  }

  async init() {
    try {
      const response = await fetch(`/api/products/${this.productId}/recommendations`);
      const data = await response.json();
      
      if (response.ok) {
        this.renderRecommendations(data);
      } else {
        console.error('Error:', data.message);
      }
    } catch (error) {
      console.error('Failed to fetch recommendations:', error);
    }
  }

  renderRecommendations(data) {
    const html = data.recommendations.map(rec => `
      <div class="recommendation-item">
        <div class="product-image">
          <img src="${rec.thumbnailUrl || 'default.jpg'}" alt="${rec.productName}">
        </div>
        <div class="product-info">
          <h3>${rec.productName}</h3>
          <p class="price">${rec.price.toLocaleString()}đ</p>
          <p class="reason">${rec.reason}</p>
          <button class="view-btn" onclick="window.location.href='/products/${rec.productId}'">
            Xem chi tiết
          </button>
        </div>
      </div>
    `).join('');

    this.recommendationsContainer.innerHTML = `
      <div class="ai-badge">🤖 AI Recommendations</div>
      <div class="recommendations-list">
        ${html}
      </div>
      <p class="ai-status">${data.aiEnabled ? '✓ Sử dụng AI' : '• Gợi ý mặc định'}</p>
    `;
  }
}

// Usage: new ProductDetailPage(1);

// =====================================================
// 3. ANGULAR EXAMPLE
// =====================================================

// product-detail.component.ts
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ProductService } from './services/product.service';

interface RecommendedProduct {
  productId: number;
  productName: string;
  price: number;
  thumbnailUrl: string;
  reason: string;
}

interface RecommendationResponse {
  recommendations: RecommendedProduct[];
  aiEnabled: boolean;
  message: string;
}

@Component({
  selector: 'app-product-detail',
  templateUrl: './product-detail.component.html',
  styleUrls: ['./product-detail.component.scss']
})
export class ProductDetailComponent implements OnInit {
  productId: number;
  recommendations: RecommendedProduct[] = [];
  loading = false;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private productService: ProductService
  ) {}

  ngOnInit() {
    this.productId = this.route.snapshot.params['id'];
    this.loadRecommendations();
  }

  loadRecommendations() {
    this.loading = true;
    this.productService.getRecommendations(this.productId).subscribe({
      next: (response: RecommendationResponse) => {
        this.recommendations = response.recommendations;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load recommendations';
        this.loading = false;
      }
    });
  }

  navigateToProduct(productId: number) {
    window.location.href = `/products/${productId}`;
  }
}

// product-detail.component.html
<div class="recommendations-section">
  <h2>🤖 AI Gợi ý cho bạn</h2>
  
  <div *ngIf="loading" class="loading">Đang tải...</div>
  
  <div *ngIf="error" class="error-message">{{ error }}</div>
  
  <div *ngIf="recommendations.length > 0" class="recommendations-grid">
    <div *ngFor="let rec of recommendations" class="recommendation-card">
      <img [src]="rec.thumbnailUrl" [alt]="rec.productName" />
      <h3>{{ rec.productName }}</h3>
      <p class="price">{{ rec.price | currency }}</p>
      <p class="reason">{{ rec.reason }}</p>
      <button (click)="navigateToProduct(rec.productId)">Xem chi tiết</button>
    </div>
  </div>
</div>

// product.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  constructor(private http: HttpClient) {}

  getRecommendations(productId: number) {
    return this.http.get(`/api/products/${productId}/recommendations`);
  }
}

// =====================================================
// 4. VUE EXAMPLE
// =====================================================

// ProductDetail.vue
<template>
  <div class="product-detail">
    <div class="recommendations-section">
      <h2>🤖 Sản phẩm được AI gợi ý</h2>
      
      <div v-if="loading" class="loading">Đang tải gợi ý...</div>
      
      <div v-if="error" class="error">{{ error }}</div>
      
      <div v-if="recommendations.length > 0" class="grid">
        <div v-for="rec in recommendations" :key="rec.productId" class="card">
          <img :src="rec.thumbnailUrl" :alt="rec.productName" />
          <h3>{{ rec.productName }}</h3>
          <p class="price">{{ formatPrice(rec.price) }}đ</p>
          <p class="reason">{{ rec.reason }}</p>
          <button @click="goToProduct(rec.productId)">Xem chi tiết</button>
        </div>
      </div>
      
      <div v-else-if="!loading" class="no-recommendations">
        Không có gợi ý nào
      </div>
      
      <p class="ai-status">
        {{ aiEnabled ? '✓ Sử dụng AI' : '• Gợi ý mặc định' }}
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();
const productId = ref(route.params.id);
const recommendations = ref([]);
const loading = ref(false);
const error = ref(null);
const aiEnabled = ref(false);

const fetchRecommendations = async () => {
  loading.value = true;
  try {
    const response = await fetch(`/api/products/${productId.value}/recommendations`);
    const data = await response.json();
    
    recommendations.value = data.recommendations;
    aiEnabled.value = data.aiEnabled;
  } catch (err) {
    error.value = 'Failed to load recommendations';
  } finally {
    loading.value = false;
  }
};

const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN').format(price);
};

const goToProduct = (id) => {
  window.location.href = `/products/${id}`;
};

onMounted(() => {
  fetchRecommendations();
});
</script>

<style scoped>
.recommendations-section {
  padding: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  margin-top: 20px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
  margin-top: 15px;
}

.card {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 15px;
  text-align: center;
  transition: box-shadow 0.3s;
}

.card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.card img {
  max-width: 100%;
  height: 150px;
  object-fit: cover;
  border-radius: 4px;
}

.price {
  font-weight: bold;
  color: #e74c3c;
  font-size: 16px;
}

.reason {
  color: #666;
  font-size: 13px;
  margin-top: 10px;
}

button {
  background: #3498db;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  margin-top: 10px;
}

button:hover {
  background: #2980b9;
}
</style>

// =====================================================
// 5. CSS STYLING
// =====================================================

/* Shared Styles */

.recommendations-section {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 30px;
  border-radius: 12px;
  margin: 30px 0;
}

.recommendations-section h2 {
  color: #2c3e50;
  font-size: 24px;
  margin-bottom: 20px;
}

.recommendations-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.recommendation-card {
  background: white;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s, box-shadow 0.3s;
}

.recommendation-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
}

.recommendation-card img {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.recommendation-card h3 {
  padding: 15px;
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 10px 0;
}

.recommendation-card .price {
  padding: 0 15px;
  font-size: 18px;
  font-weight: bold;
  color: #e74c3c;
}

.recommendation-card .reason {
  padding: 10px 15px;
  color: #555;
  font-size: 13px;
  background: #f9f9f9;
  min-height: 40px;
}

.recommendation-card button {
  width: 100%;
  padding: 12px;
  background: #3498db;
  color: white;
  border: none;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: background 0.3s;
}

.recommendation-card button:hover {
  background: #2980b9;
}

.ai-status {
  text-align: center;
  margin-top: 20px;
  color: #666;
  font-size: 12px;
}
