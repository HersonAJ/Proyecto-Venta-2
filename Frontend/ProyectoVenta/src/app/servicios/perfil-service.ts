import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RestConstants } from '../rest-constants';
import { AuthService } from './auth-service';

export interface FidelidadData {
  hotdogsComprados: number;
  puntosAcumulados: number;
  promocionActual: number;
  promocionesPendientes: number; 
  metaPromocion: number;
  porcentajeCompletado: number;
  totalPersonasAcumulando: number;
}

export interface PerfilCompleto {
  id: number;
  nombre: string;
  email: string;
  telefono: string;
  avatarId: number;
  rol: string;
  fechaRegistro: string;
  fidelidad: FidelidadData; 
}

export interface ActualizarAvatarRequest {
  avatarId: number;
}

export interface ActualizarAvatarResponse {
  success: boolean;
  message: string;
}

@Injectable({
  providedIn: 'root',
})
export class PerfilService {

  private apiUrl: string;

  constructor(
    private http: HttpClient,
    private restConstants: RestConstants,
    private authService: AuthService
  ) {
    this.apiUrl = this.restConstants.getApiURL() + 'usuario/';
  }

  getPerfilCompleto(): Observable<PerfilCompleto> {
    const token = this.authService.getToken();

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });

    return this.http.get<PerfilCompleto>(`${this.apiUrl}perfil-completo`, { headers });
  }

  actualizarAvatar(avatarId: number): Observable<any> {
    const token = this.authService.getToken();

    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });

    const body: ActualizarAvatarRequest = { avatarId };

    return this.http.put(`${this.apiUrl}avatar`, body, { headers });
  }
}