import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { VentasManualesService, ProductoVenta, ItemVenta } from '../servicios/ventas-manuales-service';
import { AuthService } from '../servicios/auth-service';

@Component({
  selector: 'app-ventas-manuales-component',
  imports: [CommonModule, FormsModule],
  templateUrl: './ventas-manuales-component.html',
  styleUrl: './ventas-manuales-component.scss'
})
export class VentasManualesComponent implements OnInit {
  productos: ProductoVenta[] = [];
  productosFiltrados: ProductoVenta[] = [];
  itemsVenta: ItemVenta[] = [];
  loading: boolean = false;
  errorMessage: string = '';
  mensajeExito: string = '';
  filtroTipo: string = 'todos';

  constructor(
    private ventasManualesService: VentasManualesService,
    private authService: AuthService,
    private router: Router
  ) { }

  ngOnInit(): void {
    if (!this.authService.isStaff()) {
      this.router.navigate(['/inicio']);
      return;
    }

    this.cargarProductos();
  }

  cargarProductos() {
    this.loading = true;
    this.ventasManualesService.obtenerProductosParaVenta().subscribe({
      next: (response) => {
        this.loading = false;
        if (response.success && response.productos) {
          this.productos = response.productos;
          this.productosFiltrados = this.productos;
        } else {
          this.errorMessage = response.message || 'Error al cargar los productos';
        }
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = 'Error de conexión al cargar productos';
        console.error('Error cargando productos:', error);
      }
    });
  }

  agregarProducto(producto: ProductoVenta) {
    const itemExistente = this.itemsVenta.find(item => item.id === producto.id);

    if (itemExistente) {
      itemExistente.cantidad += 1;
      itemExistente.subtotal = this.ventasManualesService.calcularSubtotal(
        itemExistente.cantidad,
        itemExistente.precioUnitario
      );
    } else {
      const nuevoItem: ItemVenta = {
        id: producto.id,
        nombre: producto.nombre,
        cantidad: 1,
        precioUnitario: producto.precioBase,
        subtotal: producto.precioBase
      };
      this.itemsVenta.push(nuevoItem);
    }
  }

  modificarCantidad(item: ItemVenta, nuevaCantidad: number) {
    if (nuevaCantidad <= 0) {
      this.eliminarProducto(item.id);
      return;
    }

    item.cantidad = nuevaCantidad;
    item.subtotal = this.ventasManualesService.calcularSubtotal(item.cantidad, item.precioUnitario);
  }

  eliminarProducto(productoId: number) {
    this.itemsVenta = this.itemsVenta.filter(item => item.id !== productoId);
  }

  get totalVenta(): number {
    return this.ventasManualesService.calcularTotal(this.itemsVenta);
  }

  registrarVenta() {
    if (this.itemsVenta.length === 0) {
      this.errorMessage = 'Debe agregar al menos un producto';
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.mensajeExito = '';

    const ventaRequest = {
      items: this.itemsVenta,
      total: this.totalVenta
    };

    this.ventasManualesService.registrarVentaManual(ventaRequest).subscribe({
      next: (response) => {
        this.loading = false;
        if (response.success) {
          this.mensajeExito = response.message || 'Venta registrada exitosamente';
          this.itemsVenta = []; // Limpiar carrito

          // Auto-ocultar mensaje después de 3 segundos
          setTimeout(() => {
            this.mensajeExito = '';
          }, 3000);
        } else {
          this.errorMessage = response.message || 'Error al registrar la venta';
        }
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = 'Error de conexión al registrar venta';
        console.error('Error registrando venta:', error);
      }
    });
  }

  limpiarVenta() {
    this.itemsVenta = [];
    this.errorMessage = '';
    this.mensajeExito = '';
  }
  filtrarProductos(tipo: string) {
    this.filtroTipo = tipo;

    if (tipo === 'todos') {
      this.productosFiltrados = this.productos;
    } else {
      this.productosFiltrados = this.productos.filter(producto =>
        producto.tipo.toLowerCase() === tipo.toLowerCase()
      );
    }
  }
}