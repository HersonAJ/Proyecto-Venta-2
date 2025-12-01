import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { RestConstants } from '../rest-constants';

export interface RegisterRequest {
  nombre: string;
  email: string;
  password: string;
  telefono: string;
  avatarId: number;
  aceptaTerminos: boolean;
}

export interface RegisterResponse {
  success: boolean;
  message?: string;
  userId?: number;
  token?: string;
  error?: string;
}

@Injectable({
  providedIn: 'root',
})
export class RegistroService {
  
  private apiUrl: string;

  constructor(
    private http: HttpClient,
    private restConstant: RestConstants
  ) {
    this.apiUrl = this.restConstant.getApiURL() + 'auth/';
  }

  register(registerData: RegisterRequest): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${this.apiUrl}registro`, registerData)
    .pipe(
      catchError((error: HttpErrorResponse) => {
        let errorMessage = 'Error en el registro';

        if (error.status === 400 ) {
          errorMessage = error.error?.error || 'Datos del registro inválidos';
        } else if (error.status === 409) {
          errorMessage = 'El correo electronico ya esta registrado';
        } else {
          errorMessage = 'Error del servidor. Intente mas tarde';
        }

        const errorResponse: RegisterResponse = {
          success: false,
          error: errorMessage
        };
        return throwError(() => errorResponse);
      })
    );
  }

}
