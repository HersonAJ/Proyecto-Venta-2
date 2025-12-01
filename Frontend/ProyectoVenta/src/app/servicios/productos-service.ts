import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RestConstants } from '../rest-constants';
import { AuthService } from './auth-service';

export interface IngredienteRequest {
  nombre: string;           
  esObligatorio: boolean;
}

export interface CrearProductoRequest {
  nombre: string;
  tipo: string;  
  precioBase: number;
  descripcion?: string;
  imagenUrl?: string;
  tiempoPreparacion?: number;
  ingredientes?: IngredienteRequest[];  
}

export interface CrearProductoResponse {
  success: boolean;
  message: string;
  productoId?: number;
}

@Injectable({
  providedIn: 'root',
})
export class ProductosService {

  private apiUrl: string;

  constructor(
    private http: HttpClient,
    private restConstants: RestConstants,
    private authService: AuthService
  ) {
    this.apiUrl = this.restConstants.getApiURL() + 'productos/';
  }

  crearProducto(productoData: CrearProductoRequest): Observable<CrearProductoResponse> {
    const token = this.authService.getToken();

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });

    const payload = {
      ...productoData,
      ingredientes: productoData.ingredientes || []
    };

    return this.http.post<CrearProductoResponse>(`${this.apiUrl}crear`, payload, { headers });
  }
}