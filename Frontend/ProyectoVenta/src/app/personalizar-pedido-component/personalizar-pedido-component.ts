import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { ProductoMenu } from '../servicios/menu-service';
import { CarritoService } from '../servicios/carrito-service';

@Component({
  selector: 'app-personalizar-pedido-component',
  imports: [CommonModule, FormsModule],
  templateUrl: './personalizar-pedido-component.html',
  styleUrl: './personalizar-pedido-component.scss',
})
export class PersonalizarPedidoComponent implements OnInit {

  producto!: ProductoMenu;
  ingredientesSeleccionados: number[] = [];
  cantidad: number = 1;
  loading: boolean = false;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private carritoService: CarritoService
  ) { }

  ngOnInit() {
    this.producto = this.route.snapshot.data?.['producto'] ||
      this.route.snapshot.paramMap.get('producto') ||
      history.state.producto;

    if (!this.producto) {
      this.router.navigate(['/menu']);
      return;
    }
  }

  toggleIngrediente(ingredienteId: number) {
    const index = this.ingredientesSeleccionados.indexOf(ingredienteId);
    if (index > -1) {
      this.ingredientesSeleccionados.splice(index, 1);
    } else {
      this.ingredientesSeleccionados.push(ingredienteId);
    }
  }

  incrementarCantidad() {
    this.cantidad++;
  }

  decrementarCantidad() {
    if (this.cantidad > 1) {
      this.cantidad--;
    }
  }

  agregarAlCarrito() {
    this.loading = true;

    const personalizacion = {
      ingredientesAQuitar: this.ingredientesSeleccionados
    };

    this.carritoService.agregarProducto(
      this.producto,
      personalizacion,
      this.cantidad
    );

    setTimeout(() => {
      this.loading = false;
      this.router.navigate(['/menu']);
    }, 500);
  }

  cancelar() {
    this.router.navigate(['/menu']);
  }

  esIngredienteObligatorio(ingredienteId: number): boolean {
    const ingrediente = this.producto.ingredientes.find(i => i.id === ingredienteId);
    return ingrediente?.esObligatorio || false;
  }

  getIngredientesNoObligatorios() {
    return this.producto.ingredientes.filter(i => !i.esObligatorio);
  }

  getIngredientesObligatorios() {
    return this.producto.ingredientes.filter(i => i.esObligatorio);
  }

  getNombreIngrediente(ingredienteId: number): string {
    const ingrediente = this.producto.ingredientes.find(i => i.id === ingredienteId);
    return ingrediente?.nombre || 'Ingrediente';
  }
}