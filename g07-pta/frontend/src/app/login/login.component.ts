import {Component, inject, model} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {HeaderComponent} from '../header/header.component';
import userData from '../assets/auth.json'

export interface DialogData {
  role: string;
}

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  readonly dialogRef = inject(MatDialogRef<HeaderComponent>);
  readonly data = inject<DialogData>(MAT_DIALOG_DATA);
  users: any[] = userData;
  username: string = '';
  password: string = '';
  validLogin: boolean = true;

  isAutherized(): void {
    this.users.forEach((user: {username: string, password: string, role: string}) => {
      if (this.username === user.username && this.password === user.password && this.data.role === user.role) {
        this.dialogRef.close(true);
      }
    })
    this.validLogin = false;
  }

  noLogin(): void {
    this.dialogRef.close(false);
  }
}
