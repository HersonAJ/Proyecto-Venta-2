import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ProductosService, CrearProductoRequest, IngredienteRequest } from '../servicios/productos-service'; 
import { AuthService } from '../servicios/auth-service';

@Component({
  selector: 'app-crear-producto-component',
  imports: [CommonModule, FormsModule],
  templateUrl: './crear-producto-component.html',
  styleUrl: './crear-producto-component.scss',
})
export class CrearProductoComponent {

    imagenesDisponibles: string[] = [
    'chevere.webp',
    'torta.webp',
    'torta-jamon.webp',
    'torta-mixta.webp',
    'bebida-coca-cola.webp',
    'papas-fritas.webp'
  ];

  producto: CrearProductoRequest = {
    nombre: '',
    tipo: 'Tortas',
    precioBase: 0,
    descripcion: '',
    imagenUrl: '',
    tiempoPreparacion: 10,
    ingredientes: []
  };


  nuevoIngrediente: IngredienteRequest = {
    nombre: '',
    esObligatorio: false
  };

  
  ingredientesAgregados: IngredienteRequest[] = [];

  loading: boolean = false;
  mensajeExito: string = '';
  errorMessage: string = '';

  constructor(
    private productosService: ProductosService,
    private authService: AuthService,
    private router: Router
  ) { }

  agregarIngrediente() {
    if (!this.nuevoIngrediente.nombre.trim()) {
      this.errorMessage = 'El nombre del ingrediente es requerido';
      return;
    }

    this.ingredientesAgregados.push({ ...this.nuevoIngrediente });
    this.nuevoIngrediente = { nombre: '', esObligatorio: false };
    this.errorMessage = '';
  }

  eliminarIngrediente(index: number) {
    this.ingredientesAgregados.splice(index, 1);
  }

  crearProducto() {
    this.loading = true;
    this.mensajeExito = '';
    this.errorMessage = '';

    
    if (!this.producto.nombre.trim()) {
      this.errorMessage = 'El nombre del producto es requerido';
      this.loading = false;
      return;
    }

    if (this.producto.precioBase <= 0) {
      this.errorMessage = 'El precio debe ser mayor a 0';
      this.loading = false;
      return;
    }

    
    this.producto.ingredientes = [...this.ingredientesAgregados]; 

    this.productosService.crearProducto(this.producto).subscribe({
      next: (response) => {
        this.loading = false;
        if (response.success) {
          this.mensajeExito = `Producto creado exitosamente con ID: ${response.productoId}`;
          this.limpiarFormulario();
        } else {
          this.errorMessage = response.message;
        }
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = error.error?.message || 'Error al crear el producto';
        console.error('Error creando producto:', error);
      }
    });
  }

  limpiarFormulario() {
    this.producto = {
      nombre: '',
      tipo: 'Tortas',
      precioBase: 0,
      descripcion: '',
      imagenUrl: '',
      tiempoPreparacion: 10,
      ingredientes: []
    };
    this.ingredientesAgregados = [];
    this.nuevoIngrediente = { nombre: '', esObligatorio: false };
  }
}