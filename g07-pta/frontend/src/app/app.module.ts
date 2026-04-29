import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HeaderComponent } from './header/header.component';
import { NavbarComponent } from './navbar/navbar.component';
import {MatToolbarModule} from '@angular/material/toolbar';
import {ContentComponent} from './content/content.component';
import {MatCard, MatCardContent} from '@angular/material/card';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatButton, MatFabButton} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {provideHttpClient} from '@angular/common/http';
import {MatInput} from '@angular/material/input';
import {ScrollingModule} from '@angular/cdk/scrolling';
import {MatOption, MatSelect} from "@angular/material/select";
import { LoginComponent } from './login/login.component';
import {MatDialogActions, MatDialogContent, MatDialogTitle} from '@angular/material/dialog';

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    NavbarComponent,
    ContentComponent,
    LoginComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    MatToolbarModule,
    MatCard,
    MatCardContent,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatFabButton,
    MatIconModule,
    FormsModule,
    MatInput,
    ScrollingModule,
    MatSelect,
    MatOption,
    MatDialogActions,
    MatDialogContent,
    MatDialogTitle,
    MatButton,
  ],
  providers: [provideHttpClient()],
  bootstrap: [AppComponent]
})
export class AppModule { }
