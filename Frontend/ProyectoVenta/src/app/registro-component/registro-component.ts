import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RegistroService, RegisterRequest, RegisterResponse } from '../servicios/registro-service'; 
import { AuthService } from '../servicios/auth-service';

interface AvatarOption {
  id: number;
  imageUrl: string;  // Cambiado de 'emoji' a 'imageUrl'
  name: string;
}

@Component({
  selector: 'app-registro-component',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './registro-component.html',
  styleUrl: './registro-component.scss',
})
export class RegistroComponent {

  registerData: RegisterRequest = {
    nombre: '',
    email: '',
    password: '',
    telefono: '',
    avatarId: 1,  // ID por defecto
    aceptaTerminos: false
  };

  confirmPassword: string = '';
  loading: boolean = false;
  errorMessage: string = '';
  registroExitoso: boolean = false;
  mensajeExito: string = '';

  // AVATARES ACTUALIZADOS CON IMÁGENES
  availableAvatars: AvatarOption[] = [
    { id: 1, imageUrl: 'assets/images/avatar/1.jpg', name: 'Avatar 1' },
    { id: 2, imageUrl: 'assets/images/avatar/2.jpg', name: 'Avatar 2' },
    { id: 3, imageUrl: 'assets/images/avatar/3.jpg', name: 'Avatar 3' },
    { id: 4, imageUrl: 'assets/images/avatar/4.jpg', name: 'Avatar 4' }
  ];

  constructor(
    private registroService: RegistroService,
    private router: Router,
    private authService: AuthService
  ) {}

  get passwordMismatch(): boolean {
    return this.registerData.password !== this.confirmPassword && this.confirmPassword !== '';
  }

  selectAvatar(avatarId: number): void {
    this.registerData.avatarId = avatarId;
  }

  async onRegister(): Promise<void> {
    if (this.loading) return;

    // Validaciones
    if (this.registerData.password !== this.confirmPassword) {
      this.errorMessage = 'Las contraseñas no coinciden';
      return;
    }

    if (!this.registerData.aceptaTerminos) {
      this.errorMessage = 'Debes aceptar los términos y condiciones';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    try {
      const response = await this.registroService.register(this.registerData).toPromise();
      
      if (response?.success) {
      
        if (response.token) {
          this.authService.setToken(response.token);
        }

        this.registroExitoso = true;
        this.mensajeExito = '¡Tu cuenta ha sido creada exitosamente!';
        
      } else {
        this.errorMessage = response?.error || 'Error en el registro';
      }
    } catch (error: any) {
      this.errorMessage = error.error || 'Error de conexión con el servidor';
    } finally {
      this.loading = false;
    }
  }


  onInputChange(): void {
    if (this.errorMessage) {
      this.errorMessage = '';
    }
  }

    irAInicio(): void {
    this.router.navigate(['/inicio']);
  }

  volverARegistro(): void {
    this.registroExitoso = false;
    this.mensajeExito = '';
  }
}