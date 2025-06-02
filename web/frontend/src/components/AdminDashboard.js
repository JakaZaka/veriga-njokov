import { useContext, useState } from 'react';
import { UserContext } from '../userContext';

function AdminDashboard() {
  const userContext = useContext(UserContext);
  const [activeTab, setActiveTab] = useState('users');
  
  // Mock data za demo
  const [users] = useState([
    { _id: '1', username: 'testuser', email: 'test@example.com', role: 'user', createdAt: '2024-01-15' },
    { _id: '2', username: 'admin', email: 'admin@closy.com', role: 'admin', createdAt: '2024-01-10' }
  ]);

  // Začasno odstranimo auth check za demo
  // if (!userContext.user || userContext.user.role !== 'admin') {
  //   return <Navigate replace to="/" />;
  // }

  return (
    <div className="container mt-4">
      <div className="alert alert-info" role="alert">
        <strong>Demo Mode:</strong> Admin dashboard preview
      </div>
      
      <h2>Admin Dashboard</h2>
      
      <ul className="nav nav-tabs mb-4">
        <li className="nav-item">
          <button 
            className={`nav-link ${activeTab === 'users' ? 'active' : ''}`}
            onClick={() => setActiveTab('users')}
          >
            User Management
          </button>
        </li>
        <li className="nav-item">
          <button 
            className={`nav-link ${activeTab === 'settings' ? 'active' : ''}`}
            onClick={() => setActiveTab('settings')}
          >
            Settings
          </button>
        </li>
      </ul>

      {activeTab === 'users' && (
        <div>
          <h3>User Management</h3>
          <div className="table-responsive">
            <table className="table table-striped">
              <thead>
                <tr>
                  <th>Username</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map(user => (
                  <tr key={user._id}>
                    <td>{user.username}</td>
                    <td>{user.email}</td>
                    <td>{user.role}</td>
                    <td>
                      <button className="btn btn-sm btn-outline-primary me-1">
                        Edit
                      </button>
                      <button className="btn btn-sm btn-outline-danger">
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {activeTab === 'settings' && (
        <div>
          <h3>System Settings</h3>
          <p>Settings management interface would be here.</p>
        </div>
      )}
    </div>
  );
}

export default AdminDashboard;