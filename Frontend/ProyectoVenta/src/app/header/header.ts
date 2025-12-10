import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router, RouterLink, NavigationEnd } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../servicios/auth-service';
import { PerfilService, PerfilCompleto } from '../servicios/perfil-service';
import { BehaviorSubject, Subscription } from 'rxjs';
import { take } from 'rxjs/operators';
import { CarritoService } from '../servicios/carrito-service';
import { ViewChild, ElementRef } from '@angular/core';

@Component({
  selector: 'app-header',
  imports: [CommonModule, RouterLink],
  templateUrl: './header.html',
  styleUrl: './header.scss',
})
export class Header implements OnInit, OnDestroy {

  @ViewChild('miCuentaDropdpwn') miCuentaDropdown!: ElementRef;

  private perfilDataSubject = new BehaviorSubject<PerfilCompleto | null>(null);
  perfilData$ = this.perfilDataSubject.asObservable();

  loading = false;
  private subscriptions: Subscription[] = [];
  private navbarCollapse: any;

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

    // Suscribirse a cambios de estado de autenticación
    const authSub = this.authService.loggedIn$.subscribe((isLoggedIn) => {
      if (isLoggedIn) {
        this.cargarDatosPerfil();
      } else {
        this.perfilDataSubject.next(null);
      }
    });

    // Cerrar menú en navegación
    const routerSub = this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        this.closeNavbar();
      }
    });

    this.subscriptions.push(authSub, routerSub);
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
    this.closeNavbar();
    this.authService.logout();
    this.perfilDataSubject.next(null);
    this.router.navigate(['/inicio']);
  }

  closeNavbar(): void {
    const navbarCollapse = document.getElementById('navbarNav');
    if (navbarCollapse && navbarCollapse.classList.contains('show')) {
      if (typeof (window as any).bootstrap !== 'undefined') {
        const bsCollapse = new (window as any).bootstrap.Collapse(navbarCollapse, {
          toggle: false
        });
        bsCollapse.hide();
      } else {
        navbarCollapse.classList.remove('show');
      }
    }
  }

  closeNavbarOnClick(event?: Event): void {
    const target = event?.target as HTMLElement;
    const closestDropdownToggle = target?.closest('.dropdown-toggle');
    const closestDropdownMenu = target?.closest('.dropdown-menu');

    if (closestDropdownToggle) {
      return;
    }

    if (closestDropdownMenu) {
      this.closeNavbar();
      return;
    }

    this.closeNavbar();
  }


  ngOnDestroy() {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  toggleDropdown(event: Event): void {
    event.preventDefault();
    event.stopPropagation();

    if (window.innerWidth < 992) {
      const dropdown = this.miCuentaDropdown?.nativeElement;
      if (dropdown) {
        const isExpanded = dropdown.classList.contains('show');

        if (!isExpanded) {
          return;
        }
      }
    }
  }
}