import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from '../servicios/auth-service';

@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {
  
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): boolean {
    const role = this.authService.getUserRole();
    
    // Solo admin y trabajador pueden acceder
    if (role === 'admin' || role === 'trabajador') {
      return true;
    } else {
      this.router.navigate(['/inicio']);
      return false;
    }
  }
}