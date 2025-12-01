import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../servicios/auth-service';
import { PerfilService, PerfilCompleto } from '../servicios/perfil-service';
import { BehaviorSubject, Subscription } from 'rxjs';
import { take } from 'rxjs/operators';
import { CarritoService } from '../servicios/carrito-service';

@Component({
  selector: 'app-header',
  imports: [CommonModule, RouterLink],
  templateUrl: './header.html',
  styleUrl: './header.scss',
})
export class Header implements OnInit, OnDestroy {

  private perfilDataSubject = new BehaviorSubject<PerfilCompleto | null>(null);
  perfilData$ = this.perfilDataSubject.asObservable();

  loading = false;
  private subscriptions: Subscription[] = [];

  constructor(
    public authService: AuthService,
    public carritoService: CarritoService,
    private perfilService: PerfilService,
    private router: Router
  ) { }

  ngOnInit() {
    if (this.authService.isLoggedIn()) {
      this.cargarDatosPerfil();
    }

    const sub = this.authService.loggedIn$.subscribe((isLoggedIn) => {
      if (isLoggedIn) {
        this.cargarDatosPerfil();
      } else {
        this.perfilDataSubject.next(null);
      }
    });

    this.subscriptions.push(sub);
  }

  cargarDatosPerfil() {
    if (this.authService.isLoggedIn() && !this.loading) {
      this.loading = true;

      this.perfilService.getPerfilCompleto().pipe(take(1)).subscribe({
        next: (data) => {
          this.perfilDataSubject.next(data);
          this.loading = false;
        },
        error: (error) => {
          console.error('Error cargando datos del perfil: ', error);
          this.perfilDataSubject.next(null);
          this.loading = false;
        }
      });
    }
  }

  logout(): void {
    this.authService.logout();
    this.perfilDataSubject.next(null);
    this.router.navigate(['/inicio']);
  }

  ngOnDestroy() {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}