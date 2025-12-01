import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RestConstants } from '../rest-constants';

export interface IngredienteMenu {
  id: number;
  nombre: string;
  esObligatorio: boolean;
}

export interface ProductoMenu {
  id: number;
  nombre: string;
  tipo: string;
  precioBase: number;
  imagenUrl?: string;
  descripcion?: string;
  ingredientes: IngredienteMenu[];

}
export interface MenuResponse {
  success: boolean;
  menu: ProductoMenu[];
  message?: string;
}

@Injectable({
  providedIn: 'root',
})
export class MenuService {

  private apiUrl: string;

  constructor(
    private http: HttpClient,
    private restConstants: RestConstants
  ) {
    this.apiUrl = this.restConstants.getApiURL() + 'menu/' 
  }
  
  obtenerMenu(): Observable<MenuResponse> {
    return this.http.get<MenuResponse>(`${this.apiUrl}obtener`);
  }

}
