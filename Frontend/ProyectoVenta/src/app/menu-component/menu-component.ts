import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MenuService, ProductoMenu } from '../servicios/menu-service';

@Component({
  selector: 'app-menu-component',
  imports: [CommonModule],
  templateUrl: './menu-component.html',
  styleUrl: './menu-component.scss',
})
export class MenuComponent implements OnInit {

  productos: ProductoMenu[] = [];
  loading: boolean = true;
  errorMessage: string = '';

  constructor(
    private menuService: MenuService,
    private router: Router
  ) { }

  ngOnInit() {
    this.cargarMenu();
  }

  cargarMenu() {
    this.loading = true;
    this.errorMessage = '';

    this.menuService.obtenerMenu().subscribe({
      next: (response) => {
        this.loading = false;
        if (response.success) {
          this.productos = response.menu;
        } else {
          this.errorMessage = response.message || 'Error al cargar el menú';
        }
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = 'Error de conexión al cargar el menú';
        console.error('Error cargando menú:', error);
      }
    });
  }

  seleccionarProducto(producto: ProductoMenu) {
    this.router.navigate(['/personalizar', producto.id], {
      state: { producto: producto }
    });
  }

  getProductosPorTipo(tipo: string): ProductoMenu[] {
    return this.productos.filter(producto => producto.tipo === tipo);
  }

  getTiposUnicos(): string[] {
    return [...new Set(this.productos.map(producto => producto.tipo))];
  }

  onImageError(event: any) {
    // Si la imagen no existe, mostrar una imagen por defecto
    event.target.src = '/assets/images/productos/default-product.webp';
  }

  getProductImageUrl(imagenUrl: string | null | undefined): string {
    if (!imagenUrl) {
      return '/assets/images/productos/default-product.webp';
    }
    return `/assets/images/productos/${imagenUrl}`;
  }
}
