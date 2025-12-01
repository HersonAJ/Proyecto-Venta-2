import { Component, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-footer-component',
  imports: [CommonModule],
  templateUrl: './footer-component.html',
  styleUrl: './footer-component.scss',
})
export class FooterComponent implements AfterViewInit {

  isProduction = false; // Cambiar a true cuando esté en producción

  ngAfterViewInit() {
    if (this.isProduction) {
      this.loadRealAdSense();
    } else {
      this.showMockAd();
    }
  }

  private loadRealAdSense() {
    const script = document.createElement('script');
    script.src = 'https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js?client=ca-pub-TU_ID_DE_ANUNCIANTE';
    script.async = true;
    document.head.appendChild(script);

    script.onload = () => {
      setTimeout(() => {
        try {
          ((window as any).adsbygoogle = (window as any).adsbygoogle || []).push({});
        } catch (e) {
          console.error('AdSense error:', e);
        }
      }, 1000);
    };
  }

  private showMockAd() {
    // Mostrar un banner simulado para desarrollo
    const adContainer = document.querySelector('.ad-container');
    if (adContainer) {
      adContainer.innerHTML = `
        <div class="mock-ad">
          <div class="mock-ad-content">
            <span> </span>
            <small> </small>
          </div>
        </div>
      `;
    }
  }
}