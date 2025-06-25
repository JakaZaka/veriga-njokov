import { useContext, useState, useEffect } from 'react';
import { UserContext } from '../userContext';
import { Navigate } from 'react-router-dom';
import '../AdminDashboard.css';

function AdminDashboard() {
  const userContext = useContext(UserContext);
  const [activeTab, setActiveTab] = useState('users');
  const [users, setUsers] = useState([]);
  const [settings, setSettings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [showConfirm, setShowConfirm] = useState(false);
  const [userToDelete, setUserToDelete] = useState(null);

  // Fetch users
  useEffect(() => {
    const fetchUsers = async () => {
      if (activeTab === 'users') {
        try {
          setLoading(true);
          const response = await fetch('/api/admin/users', {
            headers: {
              'Authorization': `Bearer ${userContext.user.token}`
            }
          });
          if (!response.ok) throw new Error(`API napaka: ${response.status}`);
          const data = await response.json();
          setUsers(data.data || data); // Support both { data: [...] } and [...] responses
          setError(null);
        } catch (err) {
          console.error('Napaka pri pridobivanju uporabnikov:', err);
          setError('Napaka pri pridobivanju uporabnikov. Poskusite ponovno.');
        } finally {
          setLoading(false);
        }
      }
    };
    fetchUsers();
  }, [activeTab, userContext.user.token]);

  // Fetch settings
  useEffect(() => {
    const fetchSettings = async () => {
      if (activeTab === 'settings') {
        try {
          setLoading(true);
          const response = await fetch('/api/admin/settings', {
            headers: {
              'Authorization': `Bearer ${userContext.user.token}`
            }
          });
          if (!response.ok) throw new Error(`API napaka: ${response.status}`);
          const data = await response.json();
          setSettings(data.data || data);
          setError(null);
        } catch (err) {
          console.error('Napaka pri pridobivanju nastavitev:', err);
          setError('Napaka pri pridobivanju nastavitev. Poskusite ponovno.');
        } finally {
          setLoading(false);
        }
      }
    };
    fetchSettings();
  }, [activeTab, userContext.user.token]);

  // Guard for user loading
  if (!userContext.user) {
    return (
      <div className="text-center my-4">
        <div className="spinner-border" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
      </div>
    );
  }
  if (userContext.user.role !== 'admin') {
    return <Navigate replace to="/" />;
  }

  // Delete user
  const handleDeleteUser = async (userId) => {
    try {
      setLoading(true);
      const response = await fetch(`/api/admin/users/${userId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${userContext.user.token}`
        }
      });
      if (!response.ok) throw new Error(`Napaka pri brisanju: ${response.status}`);
      setUsers(users.filter(user => user._id !== userId));
      setSuccess('Uporabnik uspešno izbrisan.');
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      console.error('Napaka pri brisanju uporabnika:', err);
      setError('Napaka pri brisanju uporabnika. Poskusite ponovno.');
    } finally {
      setLoading(false);
    }
  };

  // Toggle user role
  const handleToggleRole = async (userId, currentRole) => {
    const newRole = currentRole === 'admin' ? 'user' : 'admin';
    try {
      setLoading(true);
      const response = await fetch(`/api/admin/users/${userId}/role`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${userContext.user.token}`
        },
        body: JSON.stringify({ role: newRole })
      });
      if (!response.ok) throw new Error(`Napaka pri spremembi vloge: ${response.status}`);
      setUsers(users.map(user =>
        user._id === userId ? { ...user, role: newRole } : user
      ));
      setSuccess(`Vloga uporabnika spremenjena v "${newRole}".`);
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      console.error('Napaka pri spremembi vloge:', err);
      setError('Napaka pri spremembi vloge. Poskusite ponovno.');
    } finally {
      setLoading(false);
    }
  };

  // Custom confirmation modal for delete
  const ConfirmModal = () => (
    <div className="custom-modal-backdrop">
      <div className="custom-modal">
        <p>Ali res želite izbrisati tega uporabnika?</p>
        <button
          className="btn btn-danger"
          onClick={async () => {
            setShowConfirm(false);
            await handleDeleteUser(userToDelete);
          }}
        >
          Delete
        </button>
        <button
          className="btn btn-secondary"
          onClick={() => setShowConfirm(false)}
        >
          Cancel
        </button>
      </div>
    </div>
  );

  return (
    <div className="admin-dashboard-container">
      <h2>Admin Dashboard</h2>
      {error && (
        <div className="alert alert-danger" role="alert">{error}</div>
      )}
      {success && (
        <div className="alert alert-success" role="alert">{success}</div>
      )}
      {showConfirm && <ConfirmModal />}
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
      {loading && (
        <div className="text-center my-4">
          <div className="spinner-border" role="status">
            <span className="visually-hidden">Loading...</span>
          </div>
        </div>
      )}
      {activeTab === 'users' && !loading && (
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
                      <button
                        className="btn btn-sm btn-outline-primary me-1"
                        onClick={() => handleToggleRole(user._id, user.role)}
                        disabled={user._id === userContext.user._id}
                      >
                        Toggle Role
                      </button>
                      <button
                        className="btn btn-sm btn-outline-danger"
                        onClick={() => {
                          setUserToDelete(user._id);
                          setShowConfirm(true);
                        }}
                        disabled={user._id === userContext.user._id}
                      >
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
      {activeTab === 'settings' && !loading && (
        <div>
          <h3>System Settings</h3>
          <div className="table-responsive">
            <table className="table table-striped">
              <thead>
                <tr>
                  <th>Key</th>
                  <th>Value</th>
                  <th>Description</th>
                  <th>Category</th>
                </tr>
              </thead>
              <tbody>
                {settings.map(setting => (
                  <tr key={setting.key}>
                    <td>{setting.key}</td>
                    <td>{setting.value}</td>
                    <td>{setting.description}</td>
                    <td>{setting.category}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}

export default AdminDashboard;