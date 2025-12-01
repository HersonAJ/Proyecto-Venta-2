import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RestConstants } from '../rest-constants';
import { AuthService } from './auth-service';

export interface PedidoActivo {
  id: number;
  estado: string;
  total: number;
  fechaPedido: string;
  metodoPago: string;
  cantidadItems: number;
  detalles: PedidoDetalle[];
}

export interface PedidoDetalle {
  productoNombre: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

export interface MisPedidosResponse {
  success: boolean;
  pedidos: PedidoActivo[];
  message?: string;
}

export interface PedidoRequest {
  usuarioId: number;
  items: PedidoItemRequest[];
  total: number;
}

export interface PedidoItemRequest {
  productoId: number;
  cantidad: number;
  precioUnitario: number;
  personalizaciones: PersonalizacionRequest[];
}

export interface PersonalizacionRequest {
  ingredienteId: number;
  accion: 'quitar';
}

export interface PedidoResponse {
  success: boolean;
  message: string;
  pedidoId?: number;
}

@Injectable({
  providedIn: 'root',
})
export class PedidosService {

  private apiUrl: string;

  constructor(
    private http: HttpClient,
    private restConstants: RestConstants,
    private authService: AuthService
  ) {
    this.apiUrl = this.restConstants.getApiURL() + 'pedidos/';
  }

  crearPedido(pedidoData: PedidoRequest): Observable<PedidoResponse> {
    const token = this.authService.getToken();

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });

    return this.http.post<PedidoResponse>(`${this.apiUrl}crear`, pedidoData, { headers });
  }

  convertirCarritoAPedido(carrito: any, usuarioId: number): PedidoRequest {
    const items: PedidoItemRequest[] = carrito.items.map((item: any) => ({
      productoId: item.producto.id,
      cantidad: item.cantidad,
      precioUnitario: item.precio,
      personalizaciones: item.personalizacion.ingredientesAQuitar.map((ingredienteId: number) => ({
        ingredienteId: ingredienteId,
        accion: 'quitar' as const
      }))
    }));

    return {
      usuarioId: usuarioId,
      items: items,
      total: carrito.total
    };
  }

  obtenerMisPedidos(): Observable<MisPedidosResponse> {
    const token = this.authService.getToken();

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    return this.http.get<MisPedidosResponse>(`${this.apiUrl}mis-pedidos`, { headers });
  }
}