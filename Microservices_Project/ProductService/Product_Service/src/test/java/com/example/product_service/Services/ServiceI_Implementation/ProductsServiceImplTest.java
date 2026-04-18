package com.example.product_service.Services.ServiceI_Implementation;

import com.example.product_service.Entites.ProductCategory;
import com.example.product_service.Entites.Products;
import com.example.product_service.ExceptionHandlers.ProductAlreadyExistsException;
import com.example.product_service.ExceptionHandlers.ProductNotFoundException;
import com.example.product_service.Mappers.ProductMapper;
import com.example.product_service.Product_DTOs.ProductRequestDTO;
import com.example.product_service.Product_DTOs.ProductResponseDTO;
import com.example.product_service.Repositories.ProductCategoryRepo;
import com.example.product_service.Repositories.ProductsRepo;
import com.example.product_service.Services.ServiceI_Implementation.ProductsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductsServiceImplTest")
class ProductsServiceImplTest {

    @Mock
    private ProductsRepo productsRepo;

    @Mock
    private ProductCategoryRepo productCategoryRepo;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductsServiceImpl productsServiceImpl;

    private ProductRequestDTO productRequestDTO;
    private ProductResponseDTO productResponseDTO;
    private Products product;
    private ProductCategory category;

    @BeforeEach
    void setup() {
        productRequestDTO = new ProductRequestDTO();
        productRequestDTO.setModelName("Galaxy S24");
        productRequestDTO.setBrandName("Samsung");
        productRequestDTO.setPrice(BigDecimal.valueOf(79999));
        productRequestDTO.setQuantity(10);
        productRequestDTO.setDescription("Latest Samsung flagship");
        productRequestDTO.setCategory("Smartphones");
        productRequestDTO.setImageUrl("http://image-url");

        category = new ProductCategory();
        category.setId(1L);
        category.setName("Smartphones");
        category.setModelName("Galaxy S24");
        category.setBrandName("Samsung");

        product = new Products();
        product.setId(1L);
        product.setModelName("Galaxy S24");
        product.setBrandName("Samsung");
        product.setCategory(category);

        productResponseDTO = new ProductResponseDTO();
        productResponseDTO.setModelName("Galaxy S24");
        productResponseDTO.setBrandName("Samsung");
    }

    @Nested
    @DisplayName("createProductTest")
    class CreateProductTest {

        @Test
        @DisplayName("Should create product successfully")
        void testCreateProductSuccess() {
            when(productsRepo.existsByModelName("GALAXY S24")).thenReturn(false);
            when(productMapper.MapProductDtoToProduct(productRequestDTO)).thenReturn(product);
            when(productsRepo.save(product)).thenReturn(product);
            when(productMapper.MapProductToProductResponseDTO(product)).thenReturn(productResponseDTO);

            ProductResponseDTO actual = productsServiceImpl.createProduct(productRequestDTO);

            assertNotNull(actual);
            assertEquals("Galaxy S24", actual.getModelName());
            verify(productsRepo, times(1)).existsByModelName("GALAXY S24");
            verify(productsRepo, times(1)).save(product);
            verify(productMapper, times(1)).MapProductToProductResponseDTO(product);
        }

        @Test
        @DisplayName("Should throw exception when product already exists")
        void testCreateProductAlreadyExists() {
            when(productsRepo.existsByModelName("GALAXY S24")).thenReturn(true);

            ProductAlreadyExistsException ex = assertThrows(ProductAlreadyExistsException.class,
                    () -> productsServiceImpl.createProduct(productRequestDTO));

            assertEquals("Product with name GALAXY S24 already exists", ex.getMessage());
            verify(productMapper, times(0)).MapProductDtoToProduct(any());
            verify(productsRepo, times(0)).save(any());
        }
    }

    @Nested
    @DisplayName("getAllProductsTest")
    class GetAllProductsTest {
        @Test
        @DisplayName("Should return products list")
        void testGetAllProductsSuccess() {
            when(productsRepo.findAll()).thenReturn(List.of(product));
            when(productMapper.MapProductToProductResponseDTO(product)).thenReturn(productResponseDTO);

            List<ProductResponseDTO> result = productsServiceImpl.getAllProducts();

            assertEquals(1, result.size());
            verify(productsRepo, times(1)).findAll();
            verify(productMapper, times(1)).MapProductToProductResponseDTO(product);
        }

        @Test
        @DisplayName("Should throw exception when database empty")
        void testGetAllProductsEmpty() {
            when(productsRepo.findAll()).thenReturn(List.of());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> productsServiceImpl.getAllProducts());

            assertEquals("Products database is empty", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("getProductByIdTest")
    class GetProductByIdTest {
        @Test
        @DisplayName("Should return product when found")
        void testGetProductByIdSuccess() {
            when(productsRepo.findById(1L)).thenReturn(Optional.of(product));
            when(productMapper.MapProductToProductResponseDTO(product)).thenReturn(productResponseDTO);

            ProductResponseDTO actual = productsServiceImpl.getProductById(1L);

            assertEquals("Galaxy S24", actual.getModelName());
            verify(productsRepo, times(1)).findById(1L);
            verify(productMapper, times(1)).MapProductToProductResponseDTO(product);
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void testGetProductByIdNotFound() {
            when(productsRepo.findById(99L)).thenReturn(Optional.empty());

            ProductNotFoundException ex = assertThrows(ProductNotFoundException.class,
                    () -> productsServiceImpl.getProductById(99L));

            assertEquals("The Given Id Is Not Found In Products", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("updateProductTest")
    class UpdateProductTest {
        @Test
        @DisplayName("Should update product successfully")
        void testUpdateProductSuccess() {
            when(productsRepo.findById(1L)).thenReturn(Optional.of(product));
            when(productCategoryRepo.findById(1L)).thenReturn(Optional.of(category));
            when(productCategoryRepo.save(category)).thenReturn(category);
            when(productsRepo.save(product)).thenReturn(product);
            when(productMapper.MapProductToProductResponseDTO(product)).thenReturn(productResponseDTO);

            ProductResponseDTO actual = productsServiceImpl.updateProduct(1L, productRequestDTO);

            assertEquals("Galaxy S24", actual.getModelName());
            verify(productsRepo, times(1)).findById(1L);
            verify(productsRepo, times(1)).save(product);
            verify(productCategoryRepo, times(1)).save(category);
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void testUpdateProductNotFound() {
            when(productsRepo.findById(99L)).thenReturn(Optional.empty());

            ProductNotFoundException ex = assertThrows(ProductNotFoundException.class,
                    () -> productsServiceImpl.updateProduct(99L, productRequestDTO));

            assertEquals("The Given Id Is Not Found In Products", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("deleteProductTest")
    class DeleteProductTest {
        @Test
        @DisplayName("Should delete product successfully")
        void testDeleteProductSuccess() {
            when(productsRepo.findById(1L)).thenReturn(Optional.of(product));

            productsServiceImpl.deleteProduct(1L);

            verify(productsRepo, times(1)).delete(product);
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void testDeleteProductNotFound() {
            when(productsRepo.findById(99L)).thenReturn(Optional.empty());

            ProductNotFoundException ex = assertThrows(ProductNotFoundException.class,
                    () -> productsServiceImpl.deleteProduct(99L));

            assertEquals("The Given Id Is Not Found In Products", ex.getMessage());
            verify(productsRepo, times(0)).delete(any());
        }
    }

    @Nested
    @DisplayName("searchProductByKeywordTest")
    class SearchProductByKeywordTest {
        @Test
        @DisplayName("Should return products when keyword matches")
        void testSearchProductSuccess() {
            when(productsRepo.searchByKeyword("Galaxy")).thenReturn(List.of(product));
            when(productMapper.MapProductToProductResponseDTO(product)).thenReturn(productResponseDTO);

            List<ProductResponseDTO> result = productsServiceImpl.searchProductByKeyword("Galaxy");

            assertEquals(1, result.size());
            verify(productsRepo, times(1)).searchByKeyword("Galaxy");
        }

        @Test
        @DisplayName("Should throw exception when keyword is empty")
        void testSearchProductEmptyKeyword() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> productsServiceImpl.searchProductByKeyword(""));

            assertEquals("Search keyword must not be empty", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when no products found")
        void testSearchProductNotFound() {
            when(productsRepo.searchByKeyword("NonExisting")).thenReturn(List.of());

            ProductNotFoundException ex = assertThrows(ProductNotFoundException.class,
                    () -> productsServiceImpl.searchProductByKeyword("NonExisting"));

            assertEquals("No products found for keyword: NonExisting", ex.getMessage());
        }
    }
}
