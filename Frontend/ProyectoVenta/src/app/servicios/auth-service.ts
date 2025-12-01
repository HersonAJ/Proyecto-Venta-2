import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, catchError, throwError } from 'rxjs';
import { RestConstants } from '../rest-constants';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  success: boolean;
  message: string;
  token: string;
  userRole?: string;
  userName?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private apiUrl: string;
  private loggedInSubject = new BehaviorSubject<boolean>(this.hasToken());
  loggedIn$ = this.loggedInSubject.asObservable();

  constructor(
    private http: HttpClient,
    private restConstants: RestConstants
  ) {
    this.apiUrl = this.restConstants.getApiURL() + 'auth/';
  }

  login(loginData: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}login`, loginData)
      .pipe(
        catchError((error: HttpErrorResponse) => {
          if (error.status === 401) {
            const errorResponse: LoginResponse = {
              success: false,
              message: 'Correo o contraseña incorrecta',
              token: ''
            };
            return throwError(() => errorResponse);
          } else {
            const errorResponse: LoginResponse = {
              success: false,
              message: 'Error del servidor',
              token: ''
            };
            return throwError(() => errorResponse);
          }
        })
      );
  }

  setToken(token: string): void {
    localStorage.setItem('authToken', token);
    this.loggedInSubject.next(true);
  }

  getToken(): string | null {
    return localStorage.getItem('authToken');
  }

  logout(): void {
    localStorage.removeItem('authToken');
    this.loggedInSubject.next(false);
  }

  private hasToken(): boolean {
    return localStorage.getItem('authToken') !== null;
  }

  isLoggedIn(): boolean {
    return this.loggedInSubject.value;
  }

  getUserId(): number | null {
    const token = this.getToken();
    if (!token) return null;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.userId || null;
    } catch (error) {
      console.error('Error decoding token:', error);
      return null;
    }
  }

  getUserRole(): string | null {
    const token = this.getToken();
    if (!token) return null;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.role || payload.rol || null;
    } catch (error) {
      console.error('Error decoding token:', error);
      return null;
    }
  }

  getUserName(): string | null {
    const token = this.getToken();
    if (!token) return null;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.nombre || payload.name || null;
    } catch (error) {
      console.error('Error decoding token:', error);
      return null;
    }
  }

  isAdmin(): boolean {
    return this.getUserRole() === 'admin';
  }

  isTrabajador(): boolean {
    return this.getUserRole() === 'trabajador';
  }

  isStaff(): boolean {
    const role = this.getUserRole();
    return role === 'admin' || role === 'trabajador';
  }

  isCliente(): boolean {
    return this.getUserRole() === 'cliente';
  }
}