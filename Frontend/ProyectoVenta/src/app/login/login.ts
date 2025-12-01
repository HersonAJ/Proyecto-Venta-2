import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService, LoginRequest, LoginResponse } from '../servicios/auth-service';
import { PerfilService } from '../servicios/perfil-service';

@Component({
  selector: 'app-login',
  imports: [FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {

  loginData: LoginRequest = {
    email: '',
    password: ''
  };

  loading = false;
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private router: Router,
    private perfilService: PerfilService
  ) {}

  onLogin() {
    this.loading = true;
    this.errorMessage = '';

    this.authService.login(this.loginData).subscribe({
      next: (response: LoginResponse) => {
        this.loading = false;
        
if (response.success) {
  this.authService.setToken(response.token);
  this.perfilService.getPerfilCompleto().subscribe(); // dispara la cache o preload
  this.router.navigate(['/inicio']);
} else {
          this.errorMessage = response.message;
        }
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = error.message;
        console.error('Login error:', error);
      }
    });
  }

  continueAsGuest() {
    localStorage.setItem('guest', 'true');
    this.router.navigate(['/']);
  }
}

