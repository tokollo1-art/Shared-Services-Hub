<script>
	import RoleGuard from '$lib/components/RoleGuard.svelte';
	import StatusBadge from '$lib/components/StatusBadge.svelte';
	import StarRating from '$lib/components/StarRating.svelte';
	import Modal from '$lib/components/Modal.svelte';
	import ApiError from '$lib/components/ApiError.svelte';
	import { api } from '$lib/api.js';
	import { currentUser } from '$lib/stores/auth.js';
	import { toast } from '$lib/stores/toast.js';
	import { formatCurrency, formatDuration, formatDate } from '$lib/utils/formatters.js';
	import { onMount } from 'svelte';

	const user = $derived($currentUser);

	let tasks = $state([]);
	let loading = $state(true);
	let errorMessage = $state(null);

	// Post a Task Form State
	let showPostForm = $state(false);
	let posting = $state(false);
	let postError = $state(null);
	let taskTitle = $state('');
	let taskCategory = $state('Digital Operations');
	let taskBudget = $state(1000);
	let taskDuration = $state(7);
	let taskPaid = $state(true);
	let taskDescription = $state('');

	// Assign Task Modal State
	let assignModalOpen = $state(false);
	let selectedTaskToAssign = $state(null);
	let youthIdToAssign = $state('');
	let assigning = $state(false);

	// Verify Experience Modal State
	let verifyModalOpen = $state(false);
	let selectedTaskToVerify = $state(null);
	let verifyRating = $state(5);
	let verifyFeedback = $state('');
	let verifying = $state(false);

	const categories = [
		'Digital Operations',
		'Marketing & Creative',
		'Accounting & Finance',
		'IT & Technical',
		'Customer Support',
		'General Administration'
	];

	async function loadTasks() {
		loading = true;
		errorMessage = null;
		try {
			const res = await api.tasks.list();
			// Filter by smeId if present or show tasks
			tasks = Array.isArray(res) ? res : [];
		} catch (err) {
			console.error('Failed to load SME tasks:', err);
			errorMessage = err.message || 'Could not load your tasks.';
		} finally {
			loading = false;
		}
	}

	async function handlePostTask(event) {
		event?.preventDefault();
		if (!taskTitle.trim()) {
			postError = 'Please provide a task title.';
			return;
		}

		posting = true;
		postError = null;

		const payload = {
			title: taskTitle.trim(),
			description: taskDescription.trim(),
			category: taskCategory,
			budget: Number(taskBudget) || 0,
			durationDays: Number(taskDuration) || 7,
			isPaid: Boolean(taskPaid)
		};

		try {
			const created = await api.tasks.create(payload);
			toast.success(`Task "${created.title}" created successfully!`);
			showPostForm = false;
			// Reset fields
			taskTitle = '';
			taskDescription = '';
			taskBudget = 1000;
			taskDuration = 7;
			await loadTasks();
		} catch (err) {
			postError = err.message || 'Failed to post task.';
		} finally {
			posting = false;
		}
	}

	function openAssignModal(task) {
		selectedTaskToAssign = task;
		youthIdToAssign = '1'; // default youth id suggestion
		assignModalOpen = true;
	}

	async function handleAssignSubmit() {
		if (!selectedTaskToAssign || !youthIdToAssign) return;

		assigning = true;
		try {
			await api.tasks.assign(selectedTaskToAssign.id, youthIdToAssign);
			toast.success(`Task assigned to Candidate #${youthIdToAssign}! Status is now IN_PROGRESS.`);
			assignModalOpen = false;
			await loadTasks();
		} catch (err) {
			toast.error(err.message || 'Failed to assign task.');
		} finally {
			assigning = false;
		}
	}

	function openVerifyModal(task) {
		selectedTaskToVerify = task;
		verifyRating = 5;
		verifyFeedback = 'Work was delivered accurately and ahead of deadline. Very dependable.';
		verifyModalOpen = true;
	}

	async function handleVerifySubmit() {
		if (!selectedTaskToVerify) return;

		verifying = true;
		try {
			// Find experience id or verify against task
			const experienceId = selectedTaskToVerify.id; // Or experience mapped
			await api.experiences.verify(experienceId, verifyFeedback, verifyRating);
			toast.success('Experience verified and recorded into the Experience Ledger!');
			verifyModalOpen = false;
			await loadTasks();
		} catch (err) {
			toast.error(err.message || 'Failed to verify experience.');
		} finally {
			verifying = false;
		}
	}

	onMount(() => {
		loadTasks();
	});
</script>

<svelte:head>
	<title>SME Partner Dashboard | Shared Services Hub</title>
</svelte:head>

<RoleGuard allowedRoles={['SME', 'ADMIN']}>
	<div class="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
		<!-- Header -->
		<div class="rounded-3xl border border-slate-200 bg-white p-8 shadow-sm mb-8 flex flex-col md:flex-row md:items-center md:justify-between gap-6">
			<div>
				<div class="inline-flex items-center gap-1.5 rounded-full bg-amber-50 px-3 py-1 text-xs font-bold text-amber-800 border border-amber-200/80 mb-2">
					<span>✦ Registered SME Partner</span>
				</div>
				<h1 class="text-3xl font-extrabold text-slate-900 tracking-tight">
					Welcome, {user?.businessName || user?.name || 'SME Partner'}
				</h1>
				<p class="mt-1 text-sm text-slate-600 max-w-xl">
					Post high-priority tasks at zero cost. Corporates fund the youth wages, while your team gains dedicated operational capacity.
				</p>
			</div>

			<div class="flex items-center gap-3">
				<button
					type="button"
					class="inline-flex items-center gap-2 rounded-xl bg-indigo-600 px-5 py-3 text-sm font-bold text-white shadow-md shadow-indigo-500/20 hover:bg-indigo-700 transition-all active:scale-95"
					onclick={() => (showPostForm = !showPostForm)}
				>
					<svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M12 4v16m8-8H4" />
					</svg>
					<span>{showPostForm ? 'Close Form' : 'Post a New Task'}</span>
				</button>
			</div>
		</div>

		<!-- Error Banner -->
		<ApiError message={errorMessage} onretry={loadTasks} />

		<!-- Section: Post a Task Form -->
		{#if showPostForm}
			<div class="rounded-3xl border-2 border-indigo-200 bg-indigo-50/40 p-6 sm:p-8 mb-8 shadow-lg shadow-indigo-500/5 animate-in slide-in-from-top-4 duration-200">
				<div class="flex items-center justify-between pb-4 border-b border-indigo-100 mb-6">
					<div>
						<h2 class="text-xl font-bold text-slate-900">Post a New Task (Free)</h2>
						<p class="text-xs text-slate-600">Funded 100% by Corporate Enterprise Development reserves.</p>
					</div>
					<button
						type="button"
						class="text-slate-400 hover:text-slate-600 p-1"
						onclick={() => (showPostForm = false)}
						aria-label="Close form"
					>
						✕
					</button>
				</div>

				<ApiError message={postError} ondismiss={() => (postError = null)} />

				<form onsubmit={handlePostTask} class="space-y-6">
					<div class="grid grid-cols-1 md:grid-cols-2 gap-6">
						<!-- Title -->
						<div class="md:col-span-2">
							<label for="taskTitle" class="block text-xs font-bold uppercase tracking-wider text-slate-700">
								Task Title *
							</label>
							<input
								id="taskTitle"
								type="text"
								required
								maxlength="200"
								bind:value={taskTitle}
								placeholder="e.g. Social Media Design & Scheduling for Instagram"
								class="mt-1 block w-full rounded-xl border border-slate-300 bg-white px-4 py-2.5 text-sm text-slate-900 focus:border-indigo-600 focus:outline-none focus:ring-2 focus:ring-indigo-600/20"
							/>
						</div>

						<!-- Category -->
						<div>
							<label for="taskCategory" class="block text-xs font-bold uppercase tracking-wider text-slate-700">
								Category *
							</label>
							<select
								id="taskCategory"
								bind:value={taskCategory}
								class="mt-1 block w-full rounded-xl border border-slate-300 bg-white px-4 py-2.5 text-sm text-slate-900 focus:border-indigo-600 focus:outline-none focus:ring-2 focus:ring-indigo-600/20"
							>
								{#each categories as cat}
									<option value={cat}>{cat}</option>
								{/each}
							</select>
						</div>

						<!-- Budget (Corporate Funded) -->
						<div>
							<label for="taskBudget" class="block text-xs font-bold uppercase tracking-wider text-slate-700">
								Youth Wage Budget (ZAR)
							</label>
							<div class="relative mt-1">
								<span class="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3.5 text-slate-400 text-sm font-semibold">
									R
								</span>
								<input
									id="taskBudget"
									type="number"
									step="50"
									min="0"
									bind:value={taskBudget}
									class="block w-full rounded-xl border border-slate-300 bg-white pl-8 pr-4 py-2.5 text-sm text-slate-900 focus:border-indigo-600 focus:outline-none focus:ring-2 focus:ring-indigo-600/20 font-semibold"
								/>
							</div>
							<p class="mt-1 text-[11px] text-slate-500">Paid directly to the youth via corporate sponsorship.</p>
						</div>

						<!-- Duration Days -->
						<div>
							<label for="taskDuration" class="block text-xs font-bold uppercase tracking-wider text-slate-700">
								Duration (Days)
							</label>
							<input
								id="taskDuration"
								type="number"
								min="1"
								max="90"
								bind:value={taskDuration}
								class="mt-1 block w-full rounded-xl border border-slate-300 bg-white px-4 py-2.5 text-sm text-slate-900 focus:border-indigo-600 focus:outline-none focus:ring-2 focus:ring-indigo-600/20"
							/>
						</div>

						<!-- Paid Flag -->
						<div class="flex items-center pt-6">
							<label class="relative flex items-center gap-3 cursor-pointer">
								<input
									type="checkbox"
									bind:checked={taskPaid}
									class="h-4 w-4 rounded text-indigo-600 focus:ring-indigo-500 border-slate-300"
								/>
								<span class="text-sm font-semibold text-slate-700">
									Stipend Paid (Corporate Sponsored)
								</span>
							</label>
						</div>

						<!-- Description -->
						<div class="md:col-span-2">
							<label for="taskDescription" class="block text-xs font-bold uppercase tracking-wider text-slate-700">
								Detailed Description & Deliverables
							</label>
							<textarea
								id="taskDescription"
								rows="3"
								maxlength="1000"
								bind:value={taskDescription}
								placeholder="Describe the objectives, key files needed, milestones, and expected outputs..."
								class="mt-1 block w-full rounded-xl border border-slate-300 bg-white px-4 py-2.5 text-sm text-slate-900 focus:border-indigo-600 focus:outline-none focus:ring-2 focus:ring-indigo-600/20"
							></textarea>
						</div>
					</div>

					<div class="flex items-center justify-end gap-3 pt-4 border-t border-indigo-100">
						<button
							type="button"
							class="rounded-xl border border-slate-300 bg-white px-4 py-2.5 text-xs font-bold text-slate-700 hover:bg-slate-50"
							onclick={() => (showPostForm = false)}
						>
							Cancel
						</button>
						<button
							type="submit"
							disabled={posting}
							class="inline-flex items-center rounded-xl bg-indigo-600 px-6 py-2.5 text-xs font-bold text-white shadow-xs hover:bg-indigo-700 disabled:opacity-50"
						>
							{#if posting}
								<span class="animate-spin mr-2">◌</span>
								<span>Publishing...</span>
							{:else}
								<span>Publish Task to Hub</span>
							{/if}
						</button>
					</div>
				</form>
			</div>
		{/if}

		<!-- Section: My Tasks Management -->
		<div class="space-y-4">
			<div class="flex items-center justify-between">
				<div>
					<h2 class="text-2xl font-bold text-slate-900">My Posted Tasks</h2>
					<p class="text-xs text-slate-500">Track task assignments, progress, and verify completed work records.</p>
				</div>
				<span class="rounded-full bg-slate-100 px-3 py-1 text-xs font-bold text-slate-700">
					{tasks.length} Total
				</span>
			</div>

			{#if loading}
				<div class="space-y-3">
					{#each Array(3) as _}
						<div class="h-28 rounded-2xl bg-slate-200/60 animate-pulse"></div>
					{/each}
				</div>
			{:else if tasks.length === 0}
				<div class="rounded-3xl border border-dashed border-slate-300 bg-white p-12 text-center">
					<svg class="mx-auto h-12 w-12 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
					</svg>
					<h3 class="mt-2 text-sm font-semibold text-slate-900">No tasks created yet</h3>
					<p class="mt-1 text-xs text-slate-500 max-w-sm mx-auto">
						Start delegating work to skilled youth program alumni. Post your first task using the button above.
					</p>
					<div class="mt-4">
						<button
							type="button"
							class="rounded-xl bg-indigo-600 px-4 py-2 text-xs font-bold text-white hover:bg-indigo-700"
							onclick={() => (showPostForm = true)}
						>
							Post Task Now
						</button>
					</div>
				</div>
			{:else}
				<div class="space-y-4">
					{#each tasks as task (task.id)}
						<div class="rounded-2xl border border-slate-200 bg-white p-6 shadow-xs hover:border-indigo-200 transition-all flex flex-col md:flex-row md:items-center md:justify-between gap-4">
							<div class="space-y-2 flex-1">
								<div class="flex items-center gap-3 flex-wrap">
									<StatusBadge status={task.status} />
									<span class="rounded-md bg-slate-100 px-2 py-0.5 text-xs font-semibold text-slate-600">
										{task.category}
									</span>
									<span class="text-xs text-slate-400">
										Created {formatDate(task.createdAt)}
									</span>
								</div>

								<h3 class="text-lg font-bold text-slate-900">
									<a href={`/tasks/${task.id}`} class="hover:text-indigo-600 hover:underline">
										{task.title}
									</a>
								</h3>

								{#if task.description}
									<p class="text-xs text-slate-600 line-clamp-1 max-w-2xl">{task.description}</p>
								{/if}

								<div class="flex items-center gap-4 text-xs font-medium text-slate-500 pt-1">
									<span class="font-bold text-indigo-600">{formatCurrency(task.budget)}</span>
									<span>•</span>
									<span>{formatDuration(task.durationDays)}</span>
									{#if task.assignedToName}
										<span>•</span>
										<span class="text-slate-700 font-semibold">Assigned: {task.assignedToName}</span>
									{/if}
								</div>
							</div>

							<!-- Action Buttons depending on status -->
							<div class="flex items-center gap-2.5 shrink-0 pt-3 md:pt-0 border-t md:border-t-0 border-slate-100">
								{#if task.status === 'OPEN'}
									<button
										type="button"
										class="rounded-xl bg-indigo-50 border border-indigo-200 px-4 py-2 text-xs font-bold text-indigo-700 hover:bg-indigo-100 transition-colors shadow-2xs"
										onclick={() => openAssignModal(task)}
									>
										Assign to Youth
									</button>
								{:else if task.status === 'IN_PROGRESS' || task.status === 'COMPLETED'}
									<button
										type="button"
										class="rounded-xl bg-purple-600 px-4 py-2 text-xs font-bold text-white shadow-xs hover:bg-purple-700 transition-colors flex items-center gap-1.5"
										onclick={() => openVerifyModal(task)}
									>
										<span>Verify Experience</span>
										<svg class="h-3.5 w-3.5" viewBox="0 0 20 20" fill="currentColor">
											<path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
										</svg>
									</button>
								{:else if task.status === 'VERIFIED'}
									<span class="inline-flex items-center gap-1 rounded-lg bg-purple-50 px-3 py-1.5 text-xs font-bold text-purple-700 border border-purple-200">
										<span>✓ Verified & Ledger Recorded</span>
									</span>
								{/if}

								<a
									href={`/tasks/${task.id}`}
									class="rounded-xl border border-slate-200 px-3.5 py-2 text-xs font-semibold text-slate-700 hover:bg-slate-50 transition-colors"
								>
									View Details
								</a>
							</div>
						</div>
					{/each}
				</div>
			{/if}
		</div>
	</div>
</RoleGuard>

<!-- Modal: Assign Task to Youth -->
<Modal
	bind:open={assignModalOpen}
	title={`Assign Task: ${selectedTaskToAssign?.title || ''}`}
>
	<div class="space-y-4">
		<p class="text-xs text-slate-600 leading-relaxed">
			Enter the Youth Candidate ID to assign this task. Once assigned, the task status advances to
			<span class="font-bold text-amber-700">IN_PROGRESS</span>.
		</p>

		<div>
			<label for="youthIdInput" class="block text-xs font-bold uppercase tracking-wider text-slate-700">
				Candidate ID *
			</label>
			<input
				id="youthIdInput"
				type="number"
				min="1"
				required
				bind:value={youthIdToAssign}
				placeholder="e.g. 1"
				class="mt-1 block w-full rounded-xl border border-slate-300 px-4 py-2.5 text-sm text-slate-900 focus:border-indigo-600 focus:outline-none focus:ring-2 focus:ring-indigo-600/20"
			/>
		</div>
	</div>

	{#snippet footer()}
		<button
			type="button"
			class="rounded-xl border border-slate-300 bg-white px-4 py-2 text-xs font-bold text-slate-700 hover:bg-slate-50"
			onclick={() => (assignModalOpen = false)}
		>
			Cancel
		</button>
		<button
			type="button"
			disabled={assigning}
			class="rounded-xl bg-indigo-600 px-5 py-2 text-xs font-bold text-white shadow-xs hover:bg-indigo-700 disabled:opacity-50"
			onclick={handleAssignSubmit}
		>
			{assigning ? 'Assigning...' : 'Confirm Assignment'}
		</button>
	{/snippet}
</Modal>

<!-- Modal: Verify Experience -->
<Modal
	bind:open={verifyModalOpen}
	title={`Verify Work: ${selectedTaskToVerify?.title || ''}`}
>
	<div class="space-y-4">
		<p class="text-xs text-slate-600 leading-relaxed">
			Your evaluation unlocks the candidate's guaranteed corporate wage disbursement and adds permanent proof to their Experience Ledger.
		</p>

		<!-- Star Rating -->
		<div>
			<div class="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1.5">
				Performance Rating (0 to 5 Stars) *
			</div>
			<StarRating bind:rating={verifyRating} interactive={true} size="lg" />
		</div>

		<!-- Feedback -->
		<div>
			<label for="verifyFeedbackInput" class="block text-xs font-bold uppercase tracking-wider text-slate-700">
				Written Recommendation & Feedback *
			</label>
			<textarea
				id="verifyFeedbackInput"
				rows="3"
				bind:value={verifyFeedback}
				placeholder="Provide specific comments on communication, speed, quality, and deliverables..."
				class="mt-1 block w-full rounded-xl border border-slate-300 px-4 py-2.5 text-sm text-slate-900 focus:border-indigo-600 focus:outline-none focus:ring-2 focus:ring-indigo-600/20"
			></textarea>
		</div>
	</div>

	{#snippet footer()}
		<button
			type="button"
			class="rounded-xl border border-slate-300 bg-white px-4 py-2 text-xs font-bold text-slate-700 hover:bg-slate-50"
			onclick={() => (verifyModalOpen = false)}
		>
			Cancel
		</button>
		<button
			type="button"
			disabled={verifying}
			class="rounded-xl bg-purple-600 px-5 py-2 text-xs font-bold text-white shadow-xs hover:bg-purple-700 disabled:opacity-50"
			onclick={handleVerifySubmit}
		>
			{verifying ? 'Verifying...' : 'Sign & Record to Ledger'}
		</button>
	{/snippet}
</Modal>
