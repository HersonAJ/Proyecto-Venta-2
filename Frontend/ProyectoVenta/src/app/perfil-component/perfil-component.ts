import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { PerfilService, PerfilCompleto, FidelidadData } from '../servicios/perfil-service';
import { AuthService } from '../servicios/auth-service';

interface AvatarOption {
  id: number;
  emoji: string;
  name: string;
}

@Component({
  selector: 'app-perfil-component',
  imports: [CommonModule, FormsModule],
  templateUrl: './perfil-component.html',
  styleUrl: './perfil-component.scss',
})
export class PerfilComponent implements OnInit {

  perfilData: PerfilCompleto | null = null;
  loading: boolean = false;
  saving: boolean = false;
  mensajeExito: string = '';
  errorMessage: string = '';

  availableAvatars: AvatarOption[] = [
    { id: 1, emoji: '👤', name: 'Usuario' },
    { id: 2, emoji: '👨‍🍳', name: 'Chef' },
    { id: 3, emoji: '👨‍💼', name: 'Ejecutivo' },
    { id: 4, emoji: '🧑‍🎓', name: 'Estudiante' },
    { id: 5, emoji: '👨‍🔧', name: 'Técnico' },
    { id: 6, emoji: '🦸', name: 'Héroe' }
  ];

  constructor(
    private perfilService: PerfilService,
    private authService: AuthService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.cargarPerfil();
  }

  cargarPerfil() {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }

    this.loading = true;
    this.perfilService.getPerfilCompleto().subscribe({
      next: (data) => {
        this.perfilData = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error cargando perfil: ', error);
        this.errorMessage = 'Error al cargar el perfil';
        this.loading = false;
      }
    });
  }

  seleccionarAvatar(avatarId: number) {
    if (!this.perfilData) return;

    this.perfilData.avatarId = avatarId;
  }

  guardarAvatar() {
    if (!this.perfilData) return;

    this.saving = true;
    this.mensajeExito = '';
    this.errorMessage = '';

    this.perfilService.actualizarAvatar(this.perfilData.avatarId).subscribe({
      next: (response) => {
        this.saving = false;
        this.mensajeExito = 'Avatar actualizado correctamente';
      },
      error: (error) => {
        console.error('Error actualizando avatar:', error);
        this.saving = false;
        this.errorMessage = error.error || 'Error al actualizar avatar';
      }
    });
  }
}
