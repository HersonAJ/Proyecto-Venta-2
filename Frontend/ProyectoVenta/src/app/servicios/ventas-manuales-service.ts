import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RestConstants } from '../rest-constants';
import { AuthService } from './auth-service';

export interface ProductoVenta {
  id: number;
  nombre: string;
  tipo: string;
  precioBase: number;
  descripcion: string;
}

export interface ItemVenta {
  id: number;
  nombre: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

export interface VentaManualRequest {
  items: ItemVenta[];
  total: number;
}

export interface ProductosResponse {
  success: boolean;
  productos?: ProductoVenta[];
  message?: string;
}

export interface VentaManualResponse {
  success: boolean;
  message?: string;
  total?: number;
}

@Injectable({
  providedIn: 'root',
})
export class VentasManualesService {

  private apiUrl: string;

  constructor(
    private http: HttpClient,
    private restConstants: RestConstants,
    private authService: AuthService
  ) {
    this.apiUrl = this.restConstants.getApiURL() + 'trabajador/';
  }

  // Obtener productos disponibles para venta manual
  obtenerProductosParaVenta(): Observable<ProductosResponse> {
    const token = this.authService.getToken();

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    return this.http.get<ProductosResponse>(
      `${this.apiUrl}ventas-manuales/productos`,
      { headers }
    );
  }

  // Registrar una venta manual
  registrarVentaManual(ventaRequest: VentaManualRequest): Observable<VentaManualResponse> {
    const token = this.authService.getToken();

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });

    return this.http.post<VentaManualResponse>(
      `${this.apiUrl}ventas-manuales/registrar`,
      ventaRequest,
      { headers }
    );
  }

  // Método de utilidad para calcular subtotal
  calcularSubtotal(cantidad: number, precioUnitario: number): number {
    return cantidad * precioUnitario;
  }

  // Método de utilidad para calcular total de la venta
  calcularTotal(items: ItemVenta[]): number {
    return items.reduce((total, item) => total + item.subtotal, 0);
  }

  // Método de utilidad para formatear precio
  formatearPrecio(precio: number): string {
    return `Q${precio.toFixed(2)}`;
  }
}