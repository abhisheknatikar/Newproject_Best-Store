package com.bootmytool.beststore.controllers;

import java.io.InputStream;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.util.List;



import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.bootmytool.beststore.models.ProductDto;
import com.bootmytool.beststore.models.product;
import com.bootmytool.beststore.services.ProductsRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/products")
public class ProductsController {
	
	
	@Autowired
	private ProductsRepository repo;
	
	
	@GetMapping({"", "/"})
	public String showproductList (Model model) {
		List<product> products = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
		model.addAttribute("products", products);
		return "products/index";
	}
	
	
	@GetMapping("/create")
	public String showCreatePage (Model model) {
		ProductDto productDto = new ProductDto();
		model.addAttribute("productDto",productDto);
		return "products/CreateProduct";
	}
	
	
	@PostMapping("/create")
	public String createProduct(
			@Valid @ModelAttribute ProductDto productDto,
			BindingResult result
			) {
		
		if(productDto.getImageFile().isEmpty()) {
			result.addError(new FieldError("productDto", "imageFile", "The image file is required"));
		}
		
		if(result.hasErrors()) {
			return "products/CreateProduct";
		}
		

		
		MultipartFile image = productDto.getImageFile();

		// Generate a unique filename for the image
		Date createdAt = new Date(0);
		String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

		try {
		    // Define the directory where images will be saved
		    String uploadDir = "public/images/";
		    Path uploadPath = Paths.get(uploadDir);

		    // Create the directory if it doesn't exist
		    if (!Files.exists(uploadPath)) {
		        Files.createDirectories(uploadPath);
		    }

		    // Copy the image InputStream to the upload directory
		    try (InputStream inputStream = image.getInputStream()) {
		        Files.copy(inputStream, uploadPath.resolve(storageFileName), StandardCopyOption.REPLACE_EXISTING);
		    }

		} catch (Exception ex) {
		    // Log the exception or handle it appropriately
		    System.out.println("Exception occurred while saving image file: " + ex.getMessage());
		}
			
		
	   product product = new product();
	   product.setName(productDto.getName());
	   product.setBrand(productDto.getBrand());
	   product.setCategory(productDto.getCategory());
	   product.setPrice(productDto.getPrice());
	   product.setDescription(productDto.getDescription());
	   product.setCreatedAt(createdAt);
	   product.setImageFileName(storageFileName);
		
	   repo.save(product);
		
		
		return "redirect:/products";
	}
	
	@GetMapping("/edit")
	public String showEditPage(
			Model model,
			@RequestParam int id
			) {
		
		try {
			product product = repo.findById(id).get();
			model.addAttribute("product", product);
			
			
			   ProductDto productDto = new ProductDto();
			   productDto.setName(product.getName());
			   productDto.setBrand(product.getBrand());
			   productDto.setCategory(product.getCategory());
			   productDto.setPrice(product.getPrice());
			   productDto.setDescription(product.getDescription());
			   
			   model.addAttribute("productDto", productDto);
			
		}
		catch(Exception ex) {
			System.out.println("Exception: " + ex.getMessage());
			return "redirect://products";
		}
		
		
		return "products/EditProduct";
	}
	
	@PostMapping("/edit")
	public String updateProduct(
			Model model,
			@RequestParam int id,
			@Valid @ModelAttribute ProductDto productDto,
			BindingResult result
			) {
		
		try {
			product  product = repo.findById(id) .get();
			model.addAttribute("product",product);
			
			
			if (result.hasErrors()) {
				return "products/EditProduct";
			}
			if (!productDto.getImageFile().isEmpty()) {
				// delete old image
				String uploadDir = "public/images/";
				Path oldImagePath = Paths.get(uploadDir + product.getImageFileName());
				
				try {
					Files.delete(oldImagePath);
				}
				catch(Exception ex) {
					System.out.println("Exception: " + ex.getMessage());
				}
				//save new image file
				MultipartFile image = productDto.getImageFile();
				Date createdAt = new Date(0);
				String storageFileName = createdAt.getTime()+ "_" + image.getOriginalFilename();
				
				
				try(InputStream inputStream = image.getInputStream()){
					Files.copy(inputStream, Paths.get(uploadDir + storageFileName),StandardCopyOption.REPLACE_EXISTING);
				}
				product.setImageFileName(storageFileName);
			}
			
			product.setName(productDto.getName());
			product.setBrand(productDto.getBrand());
			product.setCategory(productDto.getCategory());
			product.setPrice(productDto.getPrice());
			product.setDescription(productDto.getDescription());
			
			
			repo.save(product);
			
			
		}
		catch(Exception ex) {
			System.out.println("Exception: " + ex.getMessage());
		}
		
		return "redirect:/products"; 
		
		
	}
	
	@GetMapping("/delete")
	public String deleteProduct(
			@RequestParam int id
			) {
		
		try {	
			product product = repo.findById(id).get();
			
			//delete product image
			Path imagePath = Paths.get("public/images/" + product.getImageFileName());
			try {
				Files.delete(imagePath);
			}
			catch (Exception ex) {
				System.out.println("Exception: " + ex.getMessage());

			}
			
             //delete the product
			repo.delete(product);
			
		}
		catch (Exception ex) {
			System.out.println("Exception: " + ex.getMessage());
		}
		
		return "redirect:/products";
	}
	
	
	}
	
	
	 


