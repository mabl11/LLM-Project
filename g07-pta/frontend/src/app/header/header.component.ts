import {Component, EventEmitter, inject, Output} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {LoginComponent} from '../login/login.component';

@Component({
  selector: 'app-header',
  standalone: false,
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss'
})
export class HeaderComponent {
  roles: string[] = ['default', 'HR', 'Developer', 'Finance'];
  currentRole = 'default';
  readonly dialog = inject(MatDialog);

  @Output() roleChange = new EventEmitter<string>();

  onRoleChange(role: string) {
    if (role !== 'default') {
      const dialogRef = this.dialog.open(LoginComponent, {
        data: {role: role},
      });
      dialogRef.afterClosed().subscribe(result => {
        console.log('The dialog was closed');
        if (result == true) {
          this.currentRole = role;
          this.roleChange.emit(role);
        }
      });
    } else if (role === 'default') {
      this.currentRole = role;
      this.roleChange.emit(role);
    }
  }
}
