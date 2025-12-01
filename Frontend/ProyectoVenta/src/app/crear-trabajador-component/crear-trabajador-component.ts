import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CrearTrabajadorService, CrearTrabajadorRequest } from '../servicios/crear-trabajador-service';
import { AuthService } from '../servicios/auth-service';

@Component({
  selector: 'app-crear-trabajador-component',
  imports: [CommonModule, FormsModule],
  templateUrl: './crear-trabajador-component.html',
  styleUrl: './crear-trabajador-component.scss',
})
export class CrearTrabajadorComponent implements OnInit {

  trabajador: CrearTrabajadorRequest = {
    nombre: '',
    email: '',
    password: '',
    telefono: ''
  };

  confirmarPassword: string = '';
  loading: boolean = false;
  mensajeExito: string = '';
  errorMessage: string = '';
  mostrarPassword: boolean = false;

  constructor(
    private crearTrabajadorService: CrearTrabajadorService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    // Verificar que el usuario sea admin
    if (!this.authService.isAdmin()) {
      this.router.navigate(['/inicio']);
      return;
    }
  }

  crearTrabajador() {
    this.loading = true;
    this.mensajeExito = '';
    this.errorMessage = '';

    // Validaciones
    if (!this.validarFormulario()) {
      this.loading = false;
      return;
    }

    this.crearTrabajadorService.crearTrabajador(this.trabajador).subscribe({
      next: (response) => {
        this.loading = false;
        if (response.success) {
          this.mensajeExito = response.message;
          this.limpiarFormulario();
        } else {
          this.errorMessage = response.message;
        }
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = error.error?.message || 'Error al crear el trabajador';
        console.error('Error creando trabajador:', error);
      }
    });
  }

  validarFormulario(): boolean {
    if (!this.trabajador.nombre.trim()) {
      this.errorMessage = 'El nombre es requerido';
      return false;
    }

    if (!this.trabajador.email.trim()) {
      this.errorMessage = 'El email es requerido';
      return false;
    }

    if (!this.validarEmail(this.trabajador.email)) {
      this.errorMessage = 'El formato del email no es válido';
      return false;
    }

    if (!this.trabajador.password) {
      this.errorMessage = 'La contraseña es requerida';
      return false;
    }

    if (this.trabajador.password.length < 6) {
      this.errorMessage = 'La contraseña debe tener al menos 6 caracteres';
      return false;
    }

    if (this.trabajador.password !== this.confirmarPassword) {
      this.errorMessage = 'Las contraseñas no coinciden';
      return false;
    }

    return true;
  }

  validarEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }

  toggleMostrarPassword() {
    this.mostrarPassword = !this.mostrarPassword;
  }

  limpiarFormulario() {
    this.trabajador = {
      nombre: '',
      email: '',
      password: '',
      telefono: ''
    };
    this.confirmarPassword = '';
  }

  getPasswordStrength(): string {
    const password = this.trabajador.password;
    if (!password) return '';

    if (password.length < 6) return 'débil';
    if (password.length < 8) return 'media';
    if (/[A-Z]/.test(password) && /[0-9]/.test(password)) return 'fuerte';
    return 'media';
  }

  getPasswordStrengthClass(): string {
    const strength = this.getPasswordStrength();
    switch (strength) {
      case 'débil': return 'text-danger';
      case 'media': return 'text-warning';
      case 'fuerte': return 'text-success';
      default: return 'text-secondary';
    }
  }
}