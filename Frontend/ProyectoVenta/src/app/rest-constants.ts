/*import { Injectable } from "@angular/core";

@Injectable({
  providedIn: 'root'
})
export class RestConstants {
  // URL relativa - será manejada por nginx
  public readonly API_URL = '/api/';

  public getApiURL(): string {
    return this.API_URL;
  }
}*/

import { Injectable } from "@angular/core";

@Injectable({
  providedIn: 'root'
})
export class RestConstants {
  // URL HARCODEADA para producción
  public readonly API_URL = 'https://backend-proyecto-venta-2.onrender.com/api/';

  public getApiURL(): string {
    return this.API_URL;
  }
}