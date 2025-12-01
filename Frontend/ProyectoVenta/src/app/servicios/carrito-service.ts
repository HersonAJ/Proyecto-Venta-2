import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { ProductoMenu } from './menu-service';

export interface ItemCarrito {
  producto: ProductoMenu;
  personalizacion: {
    ingredientesAQuitar: number[]; 
  };
  cantidad: number;
  precio: number;
}

export interface CarritoState {
  items: ItemCarrito[];
  total: number;
  cantidadTotal: number;
}

@Injectable({
  providedIn: 'root'
})
export class CarritoService {
  private carritoSubject: BehaviorSubject<CarritoState>;
  private readonly STORAGE_KEY = 'carrito_temporal';

  public carrito$;

  constructor() {
    // Cargar carrito desde localStorage al inicializar
    const carritoGuardado = this.cargarDesdeLocalStorage();
    this.carritoSubject = new BehaviorSubject<CarritoState>(carritoGuardado);
    this.carrito$ = this.carritoSubject.asObservable();
  }

  agregarProducto(
    producto: ProductoMenu, 
    personalizacion: { ingredientesAQuitar: number[] }, 
    cantidad: number = 1
  ): void {
    const currentState = this.carritoSubject.value;
    const nuevoItem: ItemCarrito = {
      producto,
      personalizacion,
      cantidad,
      precio: producto.precioBase 
    };

    const nuevosItems = [...currentState.items, nuevoItem];
    this.actualizarCarrito(nuevosItems);
  }

  eliminarItem(index: number): void {
    const currentState = this.carritoSubject.value;
    const nuevosItems = currentState.items.filter((_, i) => i !== index);
    this.actualizarCarrito(nuevosItems);
  }

  actualizarCantidad(index: number, nuevaCantidad: number): void {
    if (nuevaCantidad < 1) {
      this.eliminarItem(index);
      return;
    }

    const currentState = this.carritoSubject.value;
    const nuevosItems = [...currentState.items];
    nuevosItems[index].cantidad = nuevaCantidad;
    this.actualizarCarrito(nuevosItems);
  }

  limpiarCarrito(): void {
    localStorage.removeItem(this.STORAGE_KEY);
    this.actualizarCarrito([]);
  }

  getCarritoActual(): CarritoState {
    return this.carritoSubject.value;
  }

  private actualizarCarrito(items: ItemCarrito[]): void {
    const total = items.reduce((sum, item) => sum + (item.precio * item.cantidad), 0);
    const cantidadTotal = items.reduce((sum, item) => sum + item.cantidad, 0);

    const nuevoEstado: CarritoState = {
      items,
      total,
      cantidadTotal
    };

    // Guardar en localStorage y actualizar Subject
    this.guardarEnLocalStorage(nuevoEstado);
    this.carritoSubject.next(nuevoEstado);
  }

  private cargarDesdeLocalStorage(): CarritoState {
    try {
      const guardado = localStorage.getItem(this.STORAGE_KEY);
      if (guardado) {
        return JSON.parse(guardado);
      }
    } catch (error) {
      console.error('Error cargando carrito desde localStorage:', error);
    }
    
    // Estado por defecto
    return {
      items: [],
      total: 0,
      cantidadTotal: 0
    };
  }

  private guardarEnLocalStorage(state: CarritoState): void {
    try {
      localStorage.setItem(this.STORAGE_KEY, JSON.stringify(state));
    } catch (error) {
      console.error('Error guardando carrito en localStorage:', error);
    }
  }
}