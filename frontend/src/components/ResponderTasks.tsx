import React, { useEffect, useState } from 'react';
import { rescueTaskApi } from '../api';
import { RescueTask } from '../types';
import './ResponderTasks.css';

const ResponderTasks: React.FC = () => {
  const [tasks, setTasks] = useState<RescueTask[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadTasks = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await rescueTaskApi.getMyTasks();
      setTasks(data);
    } catch (err: any) {
      setError(err.message || 'Failed to load tasks');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTasks();
  }, []);

  const handleAcknowledge = async (id: number) => {
    try {
      await rescueTaskApi.acknowledge(id);
      loadTasks();
    } catch (err: any) {
      setError(err.message || 'Failed to acknowledge');
    }
  };

  const handleStatusChange = async (id: number, status: string) => {
    try {
      await rescueTaskApi.updateStatus(id, status);
      loadTasks();
    } catch (err: any) {
      setError(err.message || 'Failed to update status');
    }
  };

  if (loading) return <div className="responder-tasks-loading">Loading tasks...</div>;
  if (error) return <div className="responder-tasks-error">{error}</div>;

  return (
    <div className="responder-tasks">
      <h3 className="responder-tasks-title">My Rescue Tasks</h3>
      {tasks.length === 0 ? (
        <p className="responder-tasks-empty">No tasks assigned yet.</p>
      ) : (
        <ul className="responder-tasks-list">
          {tasks.map(task => (
            <li key={task.id} className={`responder-task-item status-${task.taskStatus.toLowerCase()}`}>
              <div className="task-header">
                <span className="task-disaster">{task.disasterTitle}</span>
                <span className={`task-status-badge ${task.taskStatus.toLowerCase()}`}>{task.taskStatus.replace('_', ' ')}</span>
              </div>
              <div className="task-location">{task.disasterLocation}</div>
              {task.description && <div className="task-desc">{task.description}</div>}
              <div className="task-meta">
                Updated: {new Date(task.updatedAt).toLocaleString()}
                {task.acknowledgedAt && (
                  <> · Acknowledged: {new Date(task.acknowledgedAt).toLocaleString()}</>
                )}
              </div>
              <div className="task-actions">
                {task.taskStatus === 'ASSIGNED' && (
                  <button className="ack-btn" onClick={() => handleAcknowledge(task.id)}>
                    Confirm Receipt &amp; Ready to Act
                  </button>
                )}
                {task.taskStatus === 'ACKNOWLEDGED' && (
                  <button className="status-btn" onClick={() => handleStatusChange(task.id, 'IN_PROGRESS')}>
                    Start Task
                  </button>
                )}
                {task.taskStatus === 'IN_PROGRESS' && (
                  <button className="status-btn complete" onClick={() => handleStatusChange(task.id, 'COMPLETED')}>
                    Mark Completed
                  </button>
                )}
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

export default ResponderTasks;
