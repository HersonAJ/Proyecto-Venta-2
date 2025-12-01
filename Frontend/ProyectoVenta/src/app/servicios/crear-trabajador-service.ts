import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RestConstants } from '../rest-constants';
import { AuthService } from './auth-service';

export interface CrearTrabajadorRequest {
  nombre: string;
  email: string;
  password: string;
  telefono?: string;
}

export interface CrearTrabajadorResponse {
  success: boolean;
  message: string;
}

@Injectable({
  providedIn: 'root',
})
export class CrearTrabajadorService {

  private apiUrl: string;

  constructor(
    private http: HttpClient,
    private restConstants: RestConstants,
    private authService: AuthService
  ) {
    this.apiUrl = this.restConstants.getApiURL() + 'admin/';
  }

  crearTrabajador(trabajadorData: CrearTrabajadorRequest): Observable<CrearTrabajadorResponse> {
    const token = this.authService.getToken();

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });

    return this.http.post<CrearTrabajadorResponse>(
      `${this.apiUrl}crear-trabajador`, 
      trabajadorData, 
      { headers }
    );
  }
}