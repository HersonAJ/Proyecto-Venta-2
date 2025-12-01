import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RestConstants } from '../rest-constants';
import { AuthService } from './auth-service';

export interface PedidoDetallePendiente {
  id: number;
  productoNombre: string;
  productoTipo: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
  personalizaciones: string;
}

export interface PedidoPendiente {
  id: number;
  fechaPedido: string;
  total: number;
  metodoPago: string;
  clienteNombre: string;
  clienteTelefono: string;
  detalles: PedidoDetallePendiente[];
}

export interface PedidosPendientesResponse {
  success: boolean;
  pedidos: PedidoPendiente[];
  message?: string;
}

export interface MarcarEntregadoResponse {
  success: boolean;
  message: string;
}

@Injectable({
  providedIn: 'root',
})
export class PedidosPendientesService {

  private apiUrl: string;

  constructor(
    private http: HttpClient,
    private restConstants: RestConstants,
    private authService: AuthService
  ) {
    this.apiUrl = this.restConstants.getApiURL() + 'trabajador/';
  }

  obtenerPedidosPendientes(): Observable<PedidosPendientesResponse> {
    const token = this.authService.getToken();

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    return this.http.get<PedidosPendientesResponse>(
      `${this.apiUrl}pedidos-pendientes`, 
      { headers }
    );
  }

  marcarPedidoComoEntregado(pedidoId: number): Observable<MarcarEntregadoResponse> {
    const token = this.authService.getToken();

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });

    return this.http.put<MarcarEntregadoResponse>(
      `${this.apiUrl}pedidos/${pedidoId}/entregado`,
      {}, // Body vacío para PUT
      { headers }
    );
  }
}